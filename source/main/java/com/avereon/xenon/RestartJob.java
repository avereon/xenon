package com.avereon.xenon;

import com.avereon.log.Log;
import com.avereon.product.ProgramFlag;
import com.avereon.product.Rb;
import com.avereon.util.*;
import com.avereon.weave.UpdateCommandBuilder;
import com.avereon.weave.UpdateFlag;
import com.avereon.weave.UpdateTask;
import com.avereon.xenon.product.ProductUpdate;
import lombok.CustomLog;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This class is used when a program restart is requested. Similar to a
 * shutdown hook, this class is run as the program (not the JVM) is exiting,
 * and allows the JVM to terminate.
 *
 * @author Mark Soderquist
 */
@CustomLog
public class RestartJob {

	@Getter
	public enum Mode {
		RESTART( false, false ),
		UPDATE( true, false ),
		MOCK_UPDATE( true, true );

		private final boolean update;

		private final boolean mock;

		Mode( boolean update, boolean mock ) {
			this.update = update;
			this.mock = mock;
		}
	}

	private static final String DELETE_SUFFIX = "delete";

	private static final Random random = new Random();

	@Getter
	private final Xenon program;

	@Getter
	private volatile Mode mode;

	@Getter
	private volatile String[] additionalParameters;

	private volatile ProcessBuilder builder;

	private final Path updateCommandFile;

	RestartJob( Xenon program ) {
		this.program = program;
		this.updateCommandFile = program.getUpdateManager().getUpdaterFolder().resolve( "update.commands.txt" );
		log.atInfo().log( "Restart job initialized." );
	}

	public void setMode( Mode mode, String... additionalParameters ) {
		if( this.mode == mode ) return;

		stageUpdater();

		this.mode = mode;
		this.additionalParameters = additionalParameters;
		configure( mode );
	}

	private void stageUpdater() {
		try {
			// Ensure the updater is staged...even if we have to wait
			program.getUpdateManager().stageUpdaterAndWait( 10, TimeUnit.SECONDS );
		} catch( Exception exception ) {
			log.atSevere().withCause( exception ).log( "Error staging updater" );
		}
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private void configure( Mode mode ) {
		switch( mode ) {
			case RESTART -> configureForRestart();
			case UPDATE, MOCK_UPDATE -> configureForUpdate();
		}

		log.atInfo().log( "Restart job configured: mode=%s command=%s", mode, TextUtil.toString( builder.command(), " " ) );
	}

	private synchronized void configureForRestart() {
		List<String> commands = ProcessCommands.forLauncher( program.getProgramParameters(), additionalParameters );

		// Create and configure the RESTART process builder
		builder = new ProcessBuilder( commands );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );
	}

	private synchronized void configureForUpdate() {
		UpdateManager manager = program.getUpdateManager();

		List<String> updaterLaunchCommands = new ArrayList<>( List.of( manager.getUpdaterLauncher().toString() ) );
		Path updaterFolder = manager.getUpdaterFolder();
		String updatingProgramText = Rb.textOr( RbKey.UPDATE, "updating", "Updating {0}", program.getCard().getName() );
		String logFolder = PathUtil.getParent( Log.getLogFile() );
		if( logFolder != null ) logFolder = logFolder.replace( "%h", System.getProperty( "user.home" ) );
		String logFile = PathUtil.resolve( logFolder, "update.%u.log" );
		boolean useDarkMode = getProgram().getWorkspaceManager().getThemeMetadata().isDark();

		if( mode.isMock() ) {
			updaterLaunchCommands = ProcessCommands.forLauncher();
			updaterFolder = Paths.get( System.getProperty( "user.dir" ) );
		}

		// Write the update commands to a file
		try {
			log.atTrace().log( "Storing update commands..." );
			Files.writeString( updateCommandFile, createUpdateCommands() );
			log.atDebug().log( "Update commands file=%s", updateCommandFile );
		} catch( IOException throwable ) {
			log.atError().withCause( throwable ).log( "Error storing update commands" );
		}

		// Create and configure the UPDATE process builder
		builder = new ProcessBuilder( updaterLaunchCommands );
		builder.directory( updaterFolder.toFile() );
		if( useDarkMode ) builder.command().add( UpdateFlag.DARK );
		builder.command().add( UpdateFlag.TITLE );
		builder.command().add( updatingProgramText );
		builder.command().add( UpdateFlag.UPDATE );
		builder.command().add( updateCommandFile.toString() );
		builder.command().add( ProgramFlag.LOG_FILE );
		builder.command().add( logFile );
		if( program.getProgramParameters().isSet( LogFlag.LOG_LEVEL ) ) {
			builder.command().add( ProgramFlag.LOG_LEVEL );
			builder.command().add( program.getProgramParameters().get( LogFlag.LOG_LEVEL ) );
		}
	}

	private String createUpdateCommands() {
		UpdateCommandBuilder ucb = new UpdateCommandBuilder();

		if( mode == Mode.UPDATE ) {
			for( ProductUpdate update : program.getProductManager().getStagedUpdates() ) {
				String key = update.getCard().getProductKey();
				String version = program.getProductManager().getProduct( key ).getCard().getVersion();
				Path backup = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version );
				Path delete = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version + "-" + DELETE_SUFFIX );

				String updatePath = update.getSource().toString().replace( File.separator, "/" );
				String deletePath = delete.toString().replace( File.separator, "/" );
				String backupPath = backup.toString().replace( File.separator, "/" );
				String targetPath = update.getTarget().toString().replace( File.separator, "/" );
				String launchPath = OperatingSystem.getJavaLauncherPath();
				String updatingProductText = Rb.textOr( RbKey.UPDATE, "updating", "Updating {0}", update.getCard().getName() );

				ucb.add( UpdateTask.HEADER + " \"" + updatingProductText + "\"" );

				// Make sure the delete path is clear
				ucb.add( UpdateTask.DELETE, deletePath );
				// ...and move the backup to the delete path
				ucb.add( UpdateTask.MOVE, backupPath, deletePath );

				// Move the current product to the backup path
				ucb.add( UpdateTask.MOVE, targetPath, backupPath );
				// ...and unpack the update
				ucb.add( UpdateTask.UNPACK, updatePath, targetPath );
				// ...and update the program launcher
				if( update.getCard().equals( program.getCard() ) ) ucb.add( UpdateTask.PERMISSIONS, "755", launchPath );

				// Cleanup
				ucb.add( UpdateTask.DELETE, deletePath );
			}
		} else if( mode == Mode.MOCK_UPDATE ) {
			String[] names = new String[]{ "Example Program", "Module W", "Module X", "Module Y", "Module Z" };
			for( String name : names ) {
				String updatingProductText = Rb.textOr( RbKey.UPDATE, "updating", "Updating {0}", name );
				boolean isProgram = name.equals( program.getCard().getName() );
				int steps = isProgram ? 9 : 3;
				steps += random.nextInt( 5 );
				ucb.add( UpdateTask.HEADER + " \"" + updatingProductText + "\"" );
				for( int step = 0; step < steps; step++ ) {
					ucb.add( (isProgram ? UpdateTask.ELEVATED_PAUSE : UpdateTask.PAUSE) + " 10 \"Step " + (step + 1) + "\"" );
				}
			}
		}

		// Add parameters to restart program
		List<String> launchCommands = new ArrayList<>();
		launchCommands.add( System.getProperty( "user.dir" ) );
		launchCommands.addAll( ProcessCommands.forLauncher( program.getProgramParameters(), additionalParameters ) );
		ucb.add( UpdateTask.LAUNCH, launchCommands );

		return ucb.toString();
	}

	public void start() {
		if( builder == null ) return;

		if( mode == Mode.UPDATE ) program.setUpdateInProgress( true );

		// Pause a moment to allow things to settle down
		ThreadUtil.pause( 200 );

		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD );
		builder.redirectError( ProcessBuilder.Redirect.DISCARD );
		//builder.redirectInput( ProcessBuilder.Redirect.INHERIT );

		int wait = 200;
		int timeout = 5000;
		int retryCount = 0;
		int retryLimit = timeout / wait;
		long timeLimit = System.currentTimeMillis() + timeout;
		Process process = null;

		do {
			try {
				log.atDebug().log( "Attempt %s of %s starting %s process...", retryCount, retryLimit, mode );
				process = builder.start();
				log.atInfo().log( "%s process started! pid=%s", mode, process.pid() );
				break;
			} catch( IOException exception ) {
				log.atWarn().withCause( exception ).log( "Error starting %s process", mode );
				ThreadUtil.pause( 200 );
			} finally {
				log.flush();
				if( process == null ) ThreadUtil.pause( wait );
			}
			retryCount++;
		} while( System.currentTimeMillis() < timeLimit );

		log.atInfo().log( "Restart job complete." );
	}

}
