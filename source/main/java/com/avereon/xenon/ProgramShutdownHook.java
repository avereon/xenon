package com.avereon.xenon;

import com.avereon.util.*;
import com.avereon.xenon.product.ProductUpdate;
import com.avereon.zenna.UpdateCommandBuilder;
import com.avereon.zenna.UpdateTask;

import java.io.File;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This shutdown hook is used when a program restart is requested. When a
 * restart is requested the program registers an instance of this shutdown
 * hook and stops the program, which triggers this shutdown hook to start
 * the program again.
 *
 * @author Mark Soderquist
 */
public class ProgramShutdownHook extends Thread {

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

	private volatile ProcessBuilder builder;

	private volatile UpdateCommandBuilder ucb;

	ProgramShutdownHook( Program program, Mode mode, String... additionalParameters ) {
		super( program.getCard().getName() + " Shutdown Hook" );
		this.program = program;
		this.mode = mode;
		this.additionalParameters = additionalParameters;
		this.updateCommandFile = program.getLogFolder().resolve( "update.commands.txt" );

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
	private synchronized ProgramShutdownHook configureForRestart() {
		List<String> commands = ProcessCommands.forModule( program.getProgramParameters(), additionalParameters );

		builder = new ProcessBuilder( commands );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		return this;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private synchronized ProgramShutdownHook configureForUpdate() {
		// In a development environment, what would the updater update?
		// In development the program is not executed from a location that looks
		// like the installed program location and therefore would not be a
		// location to update. It might be worth "updating" a mock location to
		// prove the logic, but restarting the application based on the initial
		// start parameters would not start the program at the mock location.

		//String modulePath = System.getProperty( "jdk.module.path" );
		//String moduleMain = System.getProperty( "jdk.module.main" );
		//String moduleMainClass = System.getProperty( "jdk.module.main.class" );

		// Linked programs do not have a module path
		//String updaterModulePath = mock ? modulePath : null;
		//String updaterModuleMain = com.avereon.zenna.Program.class.getModule().getName();
		//String updaterModuleMainClass = com.avereon.zenna.Program.class.getName();

		UpdateManager updater = program.getUpdateManager();
		boolean mock = mode == Mode.MOCK_UPDATE;

		List<String> updaterLaunchCommands = new ArrayList<>( List.of( updater.getUpdaterLauncher().toString() ) );
		if( mock ) updaterLaunchCommands = ProcessCommands.forModule();

		Path updaterFolder = updater.getUpdaterFolder();
		if( mock ) updaterFolder = Paths.get( System.getProperty( "user.dir" ) );

		builder = new ProcessBuilder( updaterLaunchCommands );
		builder.directory( updaterFolder.toFile() );

		String updatingProgramText = program.rb().textOr( BundleKey.UPDATE, "updating", "Updating {0}", program.getCard().getName() );

		builder.command().add( ProgramFlag.UPDATE );
		builder.command().add( updateCommandFile.toString() );
		if( program.getProgramParameters().isSet( LogFlag.LOG_LEVEL ) ) {
			builder.command().add( ProgramFlag.LOG_LEVEL );
			builder.command().add( program.getProgramParameters().get( LogFlag.LOG_LEVEL ) );
		}

		log.log( Log.DEBUG, mode + " command: " + TextUtil.toString( builder.command(), " " ) );

		ucb = new UpdateCommandBuilder();
		ucb.add( UpdateTask.LOG, updatingProgramText );

		if( mock ) {
			ucb.add( UpdateTask.HEADER + " \"Preparing update\"" );
			ucb.add( UpdateTask.PAUSE + " 500" );
			ucb.add( UpdateTask.HEADER + " \"" + updatingProgramText + "\"" );
			ucb.add( UpdateTask.PAUSE + " 2000 \"Simulating update\"" );
			ucb.add( UpdateTask.HEADER + " \"Finishing update\"" );
			ucb.add( UpdateTask.PAUSE + " 500" );
		} else {
			for( ProductUpdate update : program.getProductManager().getStagedUpdates() ) {
				String key = update.getCard().getProductKey();
				String version = program.getProductManager().getProduct( key ).getCard().getVersion();
				Path backup = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version );
				Path delete = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version + "-delete" );

				String updatePath = update.getSource().toString().replace( File.separator, "/" );
				String targetPath = update.getTarget().toString().replace( File.separator, "/" );
				String backupPath = backup.toString().replace( File.separator, "/" );
				String deletePath = delete.toString().replace( File.separator, "/" );
				String updateProductText = program.rb().textOr( BundleKey.UPDATE, "update", "Update {0}", update.getCard().getName() );

				ucb.add( UpdateTask.HEADER + " \"" + updateProductText + "\"" );
				ucb.add( UpdateTask.DELETE, deletePath );
				ucb.add( UpdateTask.MOVE, backupPath, deletePath );
				ucb.add( UpdateTask.MOVE, targetPath, backupPath );
				ucb.add( UpdateTask.UNPACK, updatePath, targetPath );
				ucb.add( UpdateTask.DELETE, deletePath );

				if( update.getCard().equals( program.getCard() ) ) {
					ucb.add( UpdateTask.PERMISSIONS, "755", OperatingSystem.getJavaLauncherPath() );
				}
			}
		}

		// Add parameters to restart program
		List<String> launchCommands = new ArrayList<>();
		launchCommands.add( System.getProperty( "user.dir" ) );
		launchCommands.addAll( ProcessCommands.forModule( program.getProgramParameters() ) );
		launchCommands.addAll( List.of( additionalParameters ) );
		ucb.add( UpdateTask.LAUNCH, launchCommands );
		//System.out.println( ucb.toString() );

		try {
			log.log( Log.TRACE, "Storing update commands..." );
			Files.writeString( updateCommandFile, ucb.toString() );
			log.log( Log.DEBUG, "Update commands stored file=" + updateCommandFile );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR, "Error storing update commands", throwable );
		}

		return this;
	}

	@Override
	public void run() {
		if( builder == null ) {
			log.log( Log.ERROR, "The process builder is null so no " + mode + " action will occur!" );
			return;
		}

		try {
			log.log( Log.INFO, "Starting " + mode + " process..." );
			System.out.println( "Starting " + mode + " process..." );
			if( mode == Mode.UPDATE ) program.setUpdateInProgress( true );
			builder.redirectOutput( ProcessBuilder.Redirect.DISCARD );
			builder.redirectError( ProcessBuilder.Redirect.DISCARD );
			builder.start();
			log.log( Log.INFO, mode + " process started!" );
			System.out.println( mode + " process started!" );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR, "Error restarting program", throwable );
			throwable.printStackTrace( System.err );
		} finally {
			Log.flush();
		}
	}

}
