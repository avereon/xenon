package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.xenon.task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This task makes a copy of the program in a temporary location to be used to
 * update the primary program location. This is needed on some platforms due to
 * locking runtime file resources. The task result is the path to the temporary
 * location.
 */
public class StageUpdaterTask extends Task<Void> {

	private static final System.Logger log = Log.get();

	private final Program program;

	StageUpdaterTask( Program program ) {
		this.program = program;
	}

	public Program getProgram() {
		return program;
	}

	@Override
	public Void call() throws Exception {
		UpdateManager manager = getProgram().getUpdateManager();

		// Cleanup from prior updates
		removePriorFolders( manager );

		// Determine where to put the updater
		Path updaterHome = manager.getUpdaterFolder();

		// Create the updater home folders
		Files.createDirectories( updaterHome );

		// Copy all the modules needed for the updater
		log.log( Log.DEBUG, "Copy " + program.getHomeFolder() + " to " + updaterHome );
		FileUtil.copy( program.getHomeFolder(), updaterHome );

		// NOTE Do not mark the updater files to be deleted on exit
		// because they need to exist for the updater to start after the JVM exits

		// Fix the permissions on the executable
		Path bin = manager.getUpdaterLauncher();
		if( !Files.exists( bin ) ) log.log( Log.WARN, "Unable to find updater executable: " + bin );
		if( !bin.toFile().canExecute() ) {
			boolean result = !bin.toFile().setExecutable( true );
			if( !result ) log.log( Log.WARN, "Unable to make updater executable: " + bin );
		}

		return null;
	}

	/**
	 * This method cleans up prior invocations of the updater.
	 *
	 * @param manager The update manager
	 * @throws IOException If an error occurs
	 */
	private void removePriorFolders( UpdateManager manager ) throws IOException {
		String prefix = manager.getPrefix();
		Files.list( manager.getUpdaterFolder() ).filter( ( p ) -> p.getFileName().toString().startsWith( prefix ) ).forEach( ( p ) -> {
			log.log( Log.DEBUG, "Delete prior updater: " + p.getFileName() );
			try {
				FileUtil.delete( p );
			} catch( IOException exception ) {
				log.log( Log.ERROR, "Unable to cleanup prior updater files", exception );
			}
		} );
	}

}
