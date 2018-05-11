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

	public ProgramShutdownHook configureForRestart( String... extraCommands ) {
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );

		List<String> commands = createProcessCommands( modulePath, moduleMain, moduleMainClass );
		commands.addAll( getProgramParameterCommands( extraCommands ) );

		builder = new ProcessBuilder( commands );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		printCommand( "Restart command: " );
		return this;
	}

	public ProgramShutdownHook configureForUpdate( String... extraCommands ) {
		// In a development environment, what would the updater update?
		// In development the program is not executed from a location that looks
		// like the installed program location and therefore would not be a
		// location to update. It might be worth "updating" a mock location to
		// prove the logic, but restarting the application based on the initial
		// start parameters would not start the program at the mock location.

		String updaterModulePath = stageUpdater();
		String updaterModuleMain = com.xeomar.annex.Program.class.getModule().getName();
		String updaterModuleMainClass = com.xeomar.annex.Program.class.getName();

		Path homeFolder = Paths.get( System.getProperty( "user.home" ) );
		Path logFile = homeFolder.relativize( program.getDataFolder().resolve( program.getCard().getArtifact() + "-updater.log" ) );
		String logFilePath = logFile.toString().replace( File.separator, "/" );

		builder = new ProcessBuilder( createProcessCommands( updaterModulePath, updaterModuleMain, updaterModuleMainClass ) );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		builder.command().add( UpdateFlag.TITLE );
		builder.command().add( "\"Updating " + program.getCard().getName() + "\"" );
		builder.command().add( UpdateFlag.LOG_FILE );
		builder.command().add( "%h/" + logFilePath );
		builder.command().add( UpdateFlag.LOG_LEVEL );
		builder.command().add( "trace" );
		builder.command().add( UpdateFlag.STREAM );

		UpdateCommandBuilder ucb = new UpdateCommandBuilder();

		ucb.add( UpdateTask.ECHO ).add( "Updating " + program.getCard().getName() ).line();

		// TODO Add parameters to update Xenon
		for( ProductUpdate update : program.getUpdateManager().getStagedUpdates() ) {
			// TODO The update object has the paths we need
			//Path file = program.getUpdateManager().getStagedUpdateFileName( update );
			//log.warn( "Preparing update: " + file );

			Path updatePack = update.getSource();

			String updatePackPath = updatePack.toString().replace( File.separator, "/" );

			// TODO Should I group the moves together? There are pros and cons.
			ucb.add( UpdateTask.ECHO ).add( "installPath" ).add( "archivePath" ).line();
			ucb.add( UpdateTask.ECHO ).add( updatePackPath ).add( "installPath" ).line();
		}

		ucb.add( UpdateTask.PAUSE ).add( "1000" ).line();

		// Add parameters to restart Xenon
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );
		List<String> commands = createProcessCommands( modulePath, moduleMain, moduleMainClass );
		commands.addAll( getProgramParameterCommands( extraCommands ) );
		//commands.add( ProgramFlag.NOUPDATECHECK );
		ucb.add( UpdateTask.LAUNCH ).add( commands ).line();

		stdInput = ucb.toString().getBytes( TextUtil.CHARSET );

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

	private List<String> createProcessCommands( String modulePath, String moduleMain, String moduleMainClass, String... extraCommands ) {
		List<String> commands = new ArrayList<>();
		commands.add( getRestartExecutablePath( program ) );

		// Add the VM parameters to the commands.
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		for( String command : runtimeBean.getInputArguments() ) {
			// Skip some commands
			if( command.equals( "exit" ) ) continue;
			if( command.equals( "abort" ) ) continue;
			if( command.startsWith( "--module-path" ) ) continue;
			if( command.startsWith( "-Djdk.module.main" ) ) continue;
			if( command.startsWith( "-Djdk.module.main.class" ) ) continue;

			if( !commands.contains( command ) ) commands.add( command );
		}

		// Add the module information
		commands.add( "-p" );
		commands.add( modulePath );
		commands.add( "-m" );
		commands.add( moduleMain + "/" + moduleMainClass );

		return commands;
	}

	private List<String> getProgramParameterCommands( String... extraCommands ) {
		Parameters extraParameters = Parameters.parse( extraCommands );

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

		List<String> commands = new ArrayList<>();

		// Add the collected flags.
		for( String flag : flags.keySet() ) {
			List<String> values = flags.get( flag );
			commands.add( flag );
			if( values.size() > 1 && !"true".equals( values.get( 0 ) ) ) commands.addAll( values );
		}

		// Add the collected URIs.
		if( uris.size() > 0 ) commands.addAll( uris );

		return commands;
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
			// Only redirect stdout and stderr
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
