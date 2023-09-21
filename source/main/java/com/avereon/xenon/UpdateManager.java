package com.avereon.xenon;

import com.avereon.product.Profile;
import com.avereon.util.OperatingSystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class UpdateManager {

	private final Xenon program;

	private final String prefix;

	private final Path updaterFolder;

	private final Path updaterLauncher;

	private StageUpdaterTask stageUpdaterTask;

	public UpdateManager( Xenon program ) {
		this.program = program;
		this.prefix = program.getCard().getArtifact() + "-updater";

		updaterLauncher = calcUpdaterLauncher( program.getHomeFolder(), program.getProductManager().getUpdatesFolder(), prefix, program.getProfile() );

		// Linux and Mac launcher is in a /bin folder. Windows is not.
		if( OperatingSystem.isWindows() ) {
			updaterFolder = updaterLauncher.getParent();
		} else {
			updaterFolder = updaterLauncher.getParent().getParent();
		}
	}

	public static Path calcUpdaterLauncher( Path home, Path updatesFolder, String prefix, String profile ) {
		// The pre-17 implementation expected java launcher to be a sub-folder of
		// the java home folder. Java home is now in a different location which
		// makes this strategy incorrect. The new strategy should be to relativize
		// the java launcher path against the program home folder.

		final Path updaterLauncher;

		Path javaLauncher = Paths.get( OperatingSystem.getJavaLauncherPath() );
		if( Profile.DEV.equals( profile ) ) {
			String updaterTarget = "target/" + prefix;
			Path updaterFolder = Paths.get( System.getProperty( "user.dir" ), updaterTarget );
			updaterLauncher = updaterFolder.resolve( OperatingSystem.getJavaLauncherName() );
		} else if( home.getRoot().equals( javaLauncher.getRoot() ) ) {
			Path updaterFolder = updatesFolder.resolve( "updater" );
			updaterLauncher = updaterFolder.resolve( home.relativize( javaLauncher ) );
		} else {
			updaterLauncher = javaLauncher;
		}

		return updaterLauncher;
	}

	public Xenon getProgram() {
		return program;
	}

	public String getPrefix() {
		return prefix;
	}

	public Path getUpdaterFolder() {
		return updaterFolder;
	}

	public Path getUpdaterLauncher() {
		return updaterLauncher;
	}

	public synchronized StageUpdaterTask stageUpdater() {
		if( stageUpdaterTask == null ) getProgram().getTaskManager().submit( stageUpdaterTask = new StageUpdaterTask( getProgram() ) );
		return stageUpdaterTask;
	}

	/**
	 * Should only be called from {@link RestartHook} to ensure the
	 * updater is ready for use.
	 *
	 * @param timeout How long to wait
	 * @param unit The time unit
	 * @throws Exception If an error occurs
	 */
	@SuppressWarnings( "SameParameterValue" )
	void stageUpdaterAndWait( int timeout, TimeUnit unit ) throws Exception {
		stageUpdater().get( timeout, unit );
	}

}
