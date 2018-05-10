package com.xeomar.xenon;

import com.xeomar.annex.UpdateFlag;
import com.xeomar.annex.UpdateTask;
import com.xeomar.product.ProductCard;
import com.xeomar.util.*;
import com.xeomar.xenon.update.ProductUpdate;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private volatile Program program;

	private volatile ProcessBuilder builder;

	private volatile byte[] stdInput;

	public ProgramShutdownHook( Program program ) {
		super( program.getCard().getName() + " Shutdown Hook" );
		this.program = program;
	}

	public ProgramShutdownHook configureForRestart( String... commands ) {
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );

		Parameters extraParameters = Parameters.parse( commands );

		// Collect program flags.
		Map<String, List<String>> flags = new HashMap<>();
		for( String name : program.getProgramParameters().getFlags() ) {
			flags.put( name, program.getProgramParameters().getValues( name ) );
		}
		for( String name : extraParameters.getFlags() ) {
			flags.put( name, extraParameters.getValues( name ) );
		}

		// Collect program URIs.
		List<String> uris = new ArrayList<>();
		for( String uri : program.getProgramParameters().getUris() ) {
			if( !uris.contains( uri ) ) uris.add( uri );
		}
		for( String uri : extraParameters.getUris() ) {
			if( !uris.contains( uri ) ) uris.add( uri );
		}

		builder = createProcessBuilder( modulePath, moduleMain, moduleMainClass );

		// Add the collected flags.
		for( String flag : flags.keySet() ) {
			builder.command().add( flag );
			for( String value : flags.get( flag ) ) {
				builder.command().add( value );
			}
		}

		// Add the collected URIs.
		if( uris.size() > 0 ) {
			for( String uri : uris ) {
				builder.command().add( uri );
			}
		}

		printCommand( "Restart command: " );
		return this;
	}

	public ProgramShutdownHook configureForUpdate( String... commands ) {
		// In a development environment, what would the updater update?
		// In development the program is not executed from a location that looks
		// like the installed program location and therefore would not be a
		// location to update. It might be worth "updating" a mock location to
		// prove the logic, but restarting the application based on the initial
		// start parameters would not start the program at the mock location.

		String modulePath = stageUpdater();
		String moduleMain = com.xeomar.annex.Program.class.getModule().getName();
		String moduleMainClass = com.xeomar.annex.Program.class.getName();

		Path homeFolder = Paths.get( System.getProperty( "user.home" ) );
		Path logFile = homeFolder.relativize( program.getDataFolder().resolve( program.getCard().getArtifact() + "-updater.log") );

		builder = createProcessBuilder( modulePath, moduleMain, moduleMainClass );
		builder.command().add( UpdateFlag.TITLE );
		builder.command().add( "\"Updating " + program.getCard().getName() + "\"" );
		builder.command().add( UpdateFlag.LOG_FILE );
		builder.command().add( "%h/" + logFile.toString().replace( "\\", "/" ) );
		builder.command().add( UpdateFlag.LOG_LEVEL );
		builder.command().add( "trace" );
		builder.command().add( UpdateFlag.STREAM );

		StringBuilder updaterCommands = new StringBuilder();
		updaterCommands.append( UpdateTask.LOG ).append( " Updating " + program.getCard().getName() + "\n" );

		for( ProductUpdate update : program.getUpdateManager().getStagedUpdates() ) {
			// TODO The update object has the paths we need
			//Path file = program.getUpdateManager().getStagedUpdateFileName( update );
			//log.warn( "Preparing update: " + file );

			// TODO Should I group the moves together? There are pros and cons.
			updaterCommands.append( UpdateTask.MOVE ).append( " installPath archivePath\n");
			updaterCommands.append( UpdateTask.UNPACK ).append( " updatePack installPath\n");
		}

		updaterCommands.append( UpdateTask.PAUSE ).append( " 1000\n" );

		// TODO Add parameters to update Xenon
		// TODO Add parameters to restart Xenon
		//Parameters extraParameters = Parameters.parse( commands );
		//builder.command().add( "--" + ProgramFlag.NOUPDATECHECK );
		stdInput = updaterCommands.toString().getBytes( TextUtil.CHARSET );

		printCommand( "Update command: " );
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
			// Determine where to put the updater
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

	private ProcessBuilder createProcessBuilder( String modulePath, String moduleMain, String moduleMainClass ) {
		ProcessBuilder builder = new ProcessBuilder( getRestartExecutablePath( program ) );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		// Add the VM parameters to the commands.
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		for( String command : runtimeBean.getInputArguments() ) {
			// Skip some commands
			if( command.equals( "exit" ) ) continue;
			if( command.equals( "abort" ) ) continue;
			if( command.startsWith( "--module-path" ) ) continue;
			if( command.startsWith( "-Djdk.module.main" ) ) continue;
			if( command.startsWith( "-Djdk.module.main.class" ) ) continue;

			if( !builder.command().contains( command ) ) builder.command().add( command );
		}

		// Add the module information
		builder.command().add( "-p" );
		builder.command().add( modulePath );
		builder.command().add( "-m" );
		builder.command().add( moduleMain + "/" + moduleMainClass );

		return builder;
	}

	private static String getRestartExecutablePath( Program service ) {
		String executablePath = OperatingSystem.getJavaExecutablePath();
		if( isWindowsLauncherFound( service ) ) executablePath = getWindowsLauncherPath( service );
		return executablePath;
	}

	private static boolean isWindowsLauncherFound( Program service ) {
		return new File( getWindowsLauncherPath( service ) ).exists();
	}

	private static String getWindowsLauncherPath( Program program ) {
		return program.getHomeFolder().toString() + File.separator + program.getCard().getArtifact() + ".exe";
	}

	private void printCommand( String label ) {
		log.debug( label + TextUtil.toString( builder.command(), " " ) );
	}

	@Override
	public void run() {
		if( builder == null ) return;

		try {
			builder.redirectOutput( ProcessBuilder.Redirect.INHERIT ).redirectError( ProcessBuilder.Redirect.INHERIT );
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
