package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.xenon.task.Task;
import lombok.CustomLog;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * This task makes a copy of the program in a temporary location to be used to
 * update the primary program location. This is needed on some platforms due to
 * locking runtime file resources. The task result is the path to the temporary
 * location.
 */
@Getter
@CustomLog
public class StageUpdaterTask extends Task<Void> {

	private final Xenon program;

	StageUpdaterTask( Xenon program ) {
		this.program = program;
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

		// Copy everything needed for the updater
		log.atFine().log( "Copy %s to %s", program.getHomeFolder(), updaterHome );
		if( !FileUtil.copy( program.getHomeFolder(), updaterHome ) ) log.atWarn().log( "Failed to stage updater at: %s", updaterHome );

		// NOTE Do not mark the updater files to be deleted on exit
		// because they need to exist for the updater to start after the JVM exits

		// Fix the permissions on the executable
		Path bin = manager.getUpdaterLauncher();
		if( Files.exists( bin ) ) {
			boolean result = bin.toFile().setExecutable( true, false );
			if( !result ) log.atWarning().log( "Unable to make updater executable: %s", bin );
		} else {
			log.atWarning().log( "Unable to find updater executable: %s", bin );
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
		if( !Files.exists( manager.getUpdaterFolder() ) ) return;
		try( Stream<Path> paths = Files.list( manager.getUpdaterFolder() ) ) {
			paths.filter( ( p ) -> p.getFileName().toString().startsWith( prefix ) ).forEach( ( p ) -> {
				log.atFine().log( "Delete prior updater file: %s", p.getFileName() );
				try {
					FileUtil.delete( p );
				} catch( IOException exception ) {
					log.atSevere().withCause( exception ).log( "Unable to cleanup prior updater files" );
				}
			} );
		}
	}

}
