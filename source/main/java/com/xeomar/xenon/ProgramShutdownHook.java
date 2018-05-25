package com.xeomar.xenon;

import com.xeomar.annex.UpdateCommandBuilder;
import com.xeomar.annex.UpdateFlag;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.*;
import com.xeomar.xenon.update.ProductUpdate;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
		UPDATE
	}

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private volatile Program program;

	private volatile Mode mode;

	private volatile ProcessBuilder builder;

	private volatile byte[] stdInput;

	ProgramShutdownHook( Program program ) {
		super( program.getCard().getName() + " Shutdown Hook" );
		this.program = program;
		this.mode = Mode.RESTART;
	}

	synchronized ProgramShutdownHook configureForRestart( String... extraCommands ) {
		mode = Mode.RESTART;

		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );

		List<String> commands = ProcessCommands.forModule( modulePath, moduleMain, moduleMainClass, program.getProgramParameters(), extraCommands );

		builder = new ProcessBuilder( commands );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		log.debug( mode + " command: " + TextUtil.toString( builder.command(), " " ) );

		return this;
	}

	synchronized ProgramShutdownHook configureForUpdate( String... extraCommands ) {
		// In a development environment, what would the updater update?
		// In development the program is not executed from a location that looks
		// like the installed program location and therefore would not be a
		// location to update. It might be worth "updating" a mock location to
		// prove the logic, but restarting the application based on the initial
		// start parameters would not start the program at the mock location.

		mode = Mode.UPDATE;

		String updaterModulePath = stageUpdater();
		String updaterModuleMain = com.xeomar.annex.Program.class.getModule().getName();
		String updaterModuleMainClass = com.xeomar.annex.Program.class.getName();

		Path homeFolder = Paths.get( System.getProperty( "user.home" ) );
		Path logFile = homeFolder.relativize( program.getDataFolder().resolve( program.getCard().getArtifact() + "-updater.log" ) );
		String logFilePath = logFile.toString().replace( File.separator, "/" );

		builder = new ProcessBuilder( ProcessCommands.forModule( updaterModulePath, updaterModuleMain, updaterModuleMainClass ) );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		builder.command().add( UpdateFlag.TITLE );
		builder.command().add( "\"Updating " + program.getCard().getName() + "\"" );
		builder.command().add( UpdateFlag.LOG_FILE );
		builder.command().add( "%h/" + logFilePath );
		builder.command().add( UpdateFlag.LOG_LEVEL );
		builder.command().add( "debug" );
		builder.command().add( UpdateFlag.STDIN );

		log.debug( mode + " command: " + TextUtil.toString( builder.command(), " " ) );

		UpdateCommandBuilder ucb = new UpdateCommandBuilder();
		ucb.add( UpdateTask.ECHO ).add( "Updating " + program.getCard().getName() ).line();

		for( ProductUpdate update : program.getUpdateManager().getStagedUpdates() ) {
			String name = update.getCard().getProductKey();
			String version = update.getCard().getVersion();
			Path archive = program.getDataFolder().resolve( "backup" ).resolve( name + "-" + version );

			String updatePath = update.getSource().toString().replace( File.separator, "/" );
			String targetPath = update.getTarget().toString().replace( File.separator, "/" );
			String archivePath = archive.toString().replace( File.separator, "/" );

//			// FIXME Apparently the move option breaks in Windows, but unpack still works
//			// FIXME Probably means something has a handle on the folder that we don't expect
//			ucb.add( UpdateTask.DELETE).add( archivePath ).line();
//			ucb.add( UpdateTask.MOVE ).add( targetPath ).add( archivePath ).line();

			ucb.add( UpdateTask.UNPACK ).add( updatePath ).add( targetPath ).line();
		}

		// Add parameters to restart Xenon
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );
		List<String> commands = ProcessCommands.forModule( modulePath, moduleMain, moduleMainClass, program.getProgramParameters(), extraCommands );
		ucb.add( UpdateTask.LAUNCH ).add( commands ).line();

		stdInput = ucb.toString().getBytes( TextUtil.CHARSET );

		return this;
	}

	/**
	 * Stage the updater in a temporary location. This will include all the
	 * modules need to run the updater. Might be easier to just copy the
	 * entire module path, even if some things are not needed. It's likely
	 * most things will be needed.
	 * <p>
	 * More than just the updater jar file will be needed because the updater
	 * is not a standalone jar anymore. This is due to the new Java module
	 * functionality which causes problems with the standalone shaded jar
	 * concept. Instead, all the modules needed to run the updater need to be
	 * copied to a temporary location and the updater run from that location.
	 *
	 * @return The stage updater module path
	 */
	private String stageUpdater() {
		List<Path> tempUpdaterModulePaths = new ArrayList<>();
		try {
			// Determine where to add the updater
			String updaterModuleFolderName = program.getCard().getArtifact() + "-updater";
			Path updaterModuleRoot = Paths.get( FileUtils.getTempDirectoryPath(), updaterModuleFolderName );
			if( program.getExecMode() == ExecMode.DEV ) updaterModuleRoot = Paths.get( System.getProperty( "user.dir" ), "target/" + updaterModuleFolderName );
			FileUtils.deleteDirectory( updaterModuleRoot.toFile() );
			Files.createDirectories( updaterModuleRoot );

			// Copy all the modules needed for the updater
			for( URI uri : JavaUtil.getModulePath() ) {
				log.debug( "Copying: " + uri );
				Path source = Paths.get( uri );
				if( Files.isDirectory( source ) ) {
					Path target = updaterModuleRoot.resolve( UUID.randomUUID().toString() );
					FileUtils.copyDirectory( source.toFile(), target.toFile() );
					tempUpdaterModulePaths.add( target );
				} else {
					Path target = updaterModuleRoot.resolve( source.getFileName() );
					FileUtils.copyFile( source.toFile(), target.toFile() );
					tempUpdaterModulePaths.add( updaterModuleRoot.resolve( source.getFileName() ) );
				}
			}
			// NOTE Deleting the updater files when the JVM exits causes the updater to fail to start
		} catch( IOException exception ) {
			log.error( "Unable to stage updater", exception );
			return null;
		}

		StringBuilder updaterModulePath = new StringBuilder( tempUpdaterModulePaths.get( 0 ).toString() );
		for( int index = 1; index < tempUpdaterModulePaths.size(); index++ ) {
			updaterModulePath.append( File.pathSeparator ).append( tempUpdaterModulePaths.get( index ).toString() );
		}

		return updaterModulePath.toString();
	}

	@Override
	public void run() {
		if( builder == null ) return;

		try {
			// Only redirect stdout and stderr
			builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
			Process process = builder.start();
			if( stdInput != null ) {
				process.getOutputStream().write( stdInput );
				process.getOutputStream().close();
			}
		} catch( IOException exception ) {
			log.error( "Error restarting program", exception );
		}
	}

}
