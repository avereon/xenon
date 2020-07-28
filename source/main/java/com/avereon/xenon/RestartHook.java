package com.avereon.xenon;

import com.avereon.util.*;
import com.avereon.xenon.product.ProductUpdate;
import com.avereon.weave.UpdateCommandBuilder;
import com.avereon.weave.UpdateFlag;
import com.avereon.weave.UpdateTask;

import java.io.File;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This thread is used when a program restart is requested. Similar to a
 * shutdown hook, this thread is run as the program (not the JVM) is exiting,
 * and allows the JVM to terminate.
 *
 * @author Mark Soderquist
 */
public class RestartHook extends Thread {

	public enum Mode {
		RESTART,
		MOCK_UPDATE,
		UPDATE
	}

	private static final Logger log = Log.get();

	private final Program program;

	private final Mode mode;

	private final String[] additionalParameters;

	private final Path updateCommandFile;

	private final Random random;

	private volatile ProcessBuilder builder;

	RestartHook( Program program, Mode mode, String... additionalParameters ) {
		super( program.getCard().getName() + " Shutdown Hook" );
		this.program = program;
		this.mode = mode;
		this.additionalParameters = additionalParameters;
		this.updateCommandFile = program.getLogFolder().resolve( "update.commands.txt" );
		this.random = new Random();

		if( mode == Mode.UPDATE || mode == Mode.MOCK_UPDATE ) {
			try {
				// Ensure the updater is staged...even if we have to wait
				program.getUpdateManager().stageUpdaterAndWait( 10, TimeUnit.SECONDS );
			} catch( Exception exception ) {
				log.log( Log.ERROR, "Error staging updater", exception );
			}
		}

		configure();
	}

	private void configure() {
		if( this.mode == Mode.RESTART ) {
			configureForRestart();
		} else {
			configureForUpdate();
		}
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private synchronized RestartHook configureForRestart() {
		List<String> commands = ProcessCommands.forLauncher( program.getProgramParameters(), additionalParameters );

		builder = new ProcessBuilder( commands );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		return this;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private synchronized RestartHook configureForUpdate() {
		UpdateManager manager = program.getUpdateManager();

		List<String> updaterLaunchCommands = new ArrayList<>( List.of( manager.getUpdaterLauncher().toString() ) );
		Path updaterFolder = manager.getUpdaterFolder();
		String updatingProgramText = program.rb().textOr( BundleKey.UPDATE, "updating", "Updating {0}", program.getCard().getName() );
		String logFolder = PathUtil.getParent( Log.getLogFile() );
		if( logFolder != null ) logFolder = logFolder.replace( "%h", System.getProperty( "user.home" ) );
		String logFile = PathUtil.resolve( logFolder, "update.%u.log" );

		if( mode == Mode.MOCK_UPDATE ) {
			updaterLaunchCommands = ProcessCommands.forLauncher();
			updaterFolder = Paths.get( System.getProperty( "user.dir" ) );
		}

		builder = new ProcessBuilder( updaterLaunchCommands );
		builder.directory( updaterFolder.toFile() );
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

		log.log( Log.DEBUG, mode + " command: " + TextUtil.toString( builder.command(), " " ) );

		try {
			log.log( Log.TRACE, "Storing update commands..." );
			Files.writeString( updateCommandFile, createUpdateCommands() );
			log.log( Log.DEBUG, "Update commands stored file=" + updateCommandFile );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR, "Error storing update commands", throwable );
		}

		return this;
	}

	private String createUpdateCommands() {
		boolean mock = mode == Mode.MOCK_UPDATE;
		UpdateCommandBuilder ucb = new UpdateCommandBuilder();

		if( mock ) {
			String[] names = new String[]{ program.getCard().getName(), "Mod W", "Mod X", "Mod Y", "Mod Z" };
			for( String name : names ) {
				String updatingProductText = program.rb().textOr( BundleKey.UPDATE, "updating", "Updating {0}", name );
				boolean isProgram = name.equals( program.getCard().getName() );
				int steps = isProgram ? 15 : 3;
				steps += random.nextInt( 5 );
				ucb.add( UpdateTask.HEADER + " \"" + updatingProductText + "\"" );
				for( int step = 0; step < steps; step++ ) {
					ucb.add( (isProgram ? UpdateTask.ELEVATED_PAUSE : UpdateTask.PAUSE) + " 10 \"Step " + (step + 1) + "\"" );
				}
			}
		} else {
			for( ProductUpdate update : program.getProductManager().getStagedUpdates() ) {
				String key = update.getCard().getProductKey();
				String version = program.getProductManager().getProduct( key ).getCard().getVersion();
				Path backup = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version );
				Path delete = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version + "-delete" );

				String updatePath = update.getSource().toString().replace( File.separator, "/" );
				String deletePath = delete.toString().replace( File.separator, "/" );
				String backupPath = backup.toString().replace( File.separator, "/" );
				String targetPath = update.getTarget().toString().replace( File.separator, "/" );
				String launchPath = OperatingSystem.getJavaLauncherPath();
				String updatingProductText = program.rb().textOr( BundleKey.UPDATE, "updating", "Updating {0}", update.getCard().getName() );

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
		}

		// Add parameters to restart program
		List<String> launchCommands = new ArrayList<>();
		launchCommands.add( System.getProperty( "user.dir" ) );
		launchCommands.addAll( ProcessCommands.forLauncher( program.getProgramParameters(), additionalParameters ) );
		ucb.add( UpdateTask.LAUNCH, launchCommands );
		//System.out.println( ucb.toString() );

		return ucb.toString();
	}

	@Override
	public void run() {
		// NOTE Because this is running as a shutdown hook, normal logging does not work

		if( builder == null ) return;

		try {
			System.out.println( "Starting " + mode + " process..." );
			if( mode == Mode.UPDATE ) program.setUpdateInProgress( true );
			builder.redirectOutput( ProcessBuilder.Redirect.DISCARD );
			builder.redirectError( ProcessBuilder.Redirect.DISCARD );
			builder.start();
			System.out.println( mode + " process started!" );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
	}

}
