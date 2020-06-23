package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This task makes a copy of the program in a temporary location to be used to
 * update the primary program location. This is needed on some platforms due to
 * locking runtime file resources. The task result is the path to the temporary
 * location.
 */
public class StageUpdaterTask extends Task <String>{

	private static final System.Logger log = Log.get();

	private final Program program;

	public StageUpdaterTask( Program program ) {
		this.program = program;
	}

	@Override
	public String call() throws Exception {
		String prefix = program.getCard().getArtifact() + "-updater-";

		// Cleanup from prior updates
		removePriorFolders( prefix );

		// Determine where to put the updater
		Path updaterHomeRoot = FileUtil.createTempFolder( prefix );
		if( program.getProfile() == Profile.DEV ) {
			updaterHomeRoot = Paths.get( System.getProperty( "user.dir" ), "target/" + program.getCard().getArtifact() + "-updater" );
		}

		// Create the updater home folders
		Files.createDirectories( updaterHomeRoot );

		// Copy all the modules needed for the updater
		log.log( Log.DEBUG, "Copy " + program.getHomeFolder() + " to " + updaterHomeRoot );
		FileUtil.copy( program.getHomeFolder(), updaterHomeRoot );

		// Fix the permissions on the executable
		Path bin = updaterHomeRoot.resolve( "bin" ).resolve( OperatingSystem.getJavaLauncherName() + OperatingSystem.getExeSuffix() );
		if( !bin.toFile().setExecutable( true, true ) ) log.log( Log.WARN, "Unable to make updater executable: " + bin );

		// NOTE Deleting the updater files when the JVM exits causes the updater to fail to start

		program.setUpdaterFolder( updaterHomeRoot );
		return updaterHomeRoot.toString();
	}

	/**
	 * This method cleans up prior invocations of the updater.
	 *
	 * @param prefix The temp folder prefix
	 * @throws IOException If an error occurs
	 */
	private void removePriorFolders( String prefix ) throws IOException {
		Files.list( FileUtil.getTempFolder() ).filter( ( p ) -> p.getFileName().toString().startsWith( prefix ) ).forEach( ( p ) -> {
			log.log( Log.INFO, "Delete prior updater: " + p.getFileName() );
			try {
				FileUtil.delete( p );
			} catch( IOException exception ) {
				log.log( Log.ERROR, "Unable to cleanup prior updater files", exception );
			}
		} );
	}

}
