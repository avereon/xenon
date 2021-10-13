package com.avereon.xenon;

import com.avereon.util.OperatingSystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class UpdateManager {

	private final Program program;

	private final String prefix;

	private final Path updaterFolder;

	private final Path updaterLauncher;

	private StageUpdaterTask stageUpdaterTask;

	UpdateManager( Program program ) {
		this.program = program;
		this.prefix = program.getCard().getArtifact() + "-updater-";

		Path javaLauncher = Paths.get( OperatingSystem.getJavaLauncherPath() );
		if( Profile.DEV.equals( program.getProfile() ) ) {
			String updaterTarget = "target/" + program.getCard().getArtifact() + "-updater";
			this.updaterFolder = Paths.get( System.getProperty( "user.dir" ), updaterTarget );
			this.updaterLauncher = javaLauncher;
		} else {
			this.updaterFolder = program.getProductManager().getUpdatesFolder().resolve( "updater" );
			if( program.getHomeFolder().getRoot().equals( javaLauncher.getRoot() ) ) {
				this.updaterLauncher = updaterFolder.resolve( program.getHomeFolder().relativize( javaLauncher ) );
			} else {
				this.updaterLauncher = javaLauncher;
			}
		}
	}

	public Program getProgram() {
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
