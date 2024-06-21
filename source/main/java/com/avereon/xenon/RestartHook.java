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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This thread is used when a program restart is requested. Similar to a shutdown hook, this thread is run as the program (not the JVM) is exiting, and allows the JVM to terminate.
 *
 * @author Mark Soderquist
 */
@CustomLog
public class RestartHook extends Thread {

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

	@Getter
	private final Xenon program;

	private final Path updateCommandFile;

	private final Random random;

	@Getter
	private volatile Mode mode;

	@Getter
	private volatile String[] additionalParameters;

	private volatile ProcessBuilder builder;

	RestartHook( Xenon program ) {
		super( program.getCard().getName() + " Shutdown Hook" );
		this.program = program;
		this.updateCommandFile = program.getLogFolder().resolve( "update.commands.txt" );
		this.random = new Random();

		stageUpdater();

		log.atInfo().log( "Restart hook created." );
	}

	public void setMode( Mode mode, String... additionalParameters ) {
		if( this.mode == mode ) return;

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
	private RestartHook configure( Mode mode ) {
		switch( mode ) {
			case RESTART -> configureForRestart();
			case UPDATE, MOCK_UPDATE -> configureForUpdate();
		}

		log.atInfo().log( "Restart hook configured: mode=%s command=%s", mode, TextUtil.toString( builder.command(), " " ) );

		return this;
	}

	private synchronized void configureForRestart() {
		List<String> commands = ProcessCommands.forLauncher( program.getProgramParameters(), additionalParameters );
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

		try {
			log.atTrace().log( "Storing update commands..." );
			Files.writeString( updateCommandFile, createUpdateCommands() );
			log.atDebug().log( "Update commands file=%s", updateCommandFile );
		} catch( IOException throwable ) {
			log.atError().withCause( throwable ).log( "Error storing update commands" );
		}

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

	@Override
	public void run() {
		if( builder == null ) return;

		// *** 2024 Jun 20: The logger is not available in the run method. ***
		// *** Using the logger in the run method does not work as expected. ***

		// TODO Should this be in a retry loop?

		try( RandomAccessFile file = new RandomAccessFile( builder.command().getFirst(), "r" ); FileChannel channel = file.getChannel() ) {
			if( mode == Mode.UPDATE ) program.setUpdateInProgress( true );
			builder.redirectOutput( ProcessBuilder.Redirect.DISCARD );
			builder.redirectError( ProcessBuilder.Redirect.DISCARD );
			//builder.redirectInput( ProcessBuilder.Redirect.INHERIT );

			Process process = builder.start();
			log.atInfo().log( "%s process started! pid=%s", mode, process.pid() );

//			FileLock lock;
//			Process process;
//			int retryCount = 0;
//			int retryLimit = 5;
//			do {
//				lock = channel.tryLock( 0L, Long.MAX_VALUE, true );
//				if( lock != null ) {
//					lock.release();
//					process = builder.start();
//					System.out.println( mode + " process started! pid=" + process.pid() );
//				} else {
//					ThreadUtil.pause( 1000 );
//				}
//			} while( lock == null && ++retryCount < retryLimit );
		} catch( IOException exception ) {
			log.atWarn().withCause( exception ).log( "Error starting %s process", mode );
			exception.printStackTrace( System.err );
		}

	}

}
