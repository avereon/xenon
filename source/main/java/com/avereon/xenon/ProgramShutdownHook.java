package com.avereon.xenon;

import com.avereon.util.*;
import com.avereon.xenon.product.ProductUpdate;
import com.avereon.zenna.UpdateCommandBuilder;
import com.avereon.zenna.UpdateFlag;
import com.avereon.zenna.UpdateTask;
import java.lang.System.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

	private static final Logger log = Log.get();

	private volatile Program program;

	private volatile Mode mode;

	private volatile ProcessBuilder builder;

	private volatile byte[] updateCommandsForStdIn;

	ProgramShutdownHook( Program program ) {
		super( program.getCard().getName() + " Shutdown Hook" );
		this.program = program;
		this.mode = Mode.RESTART;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	synchronized ProgramShutdownHook configureForRestart( String... extraCommands ) {
		mode = Mode.RESTART;

		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );

		List<String> commands = ProcessCommands.forModule(
			OperatingSystem.getJavaExecutablePath(),
			modulePath,
			moduleMain,
			moduleMainClass,
			program.getProgramParameters(),
			extraCommands
		);

		builder = new ProcessBuilder( commands );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		return this;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	synchronized ProgramShutdownHook configureForUpdate( String... restartCommands ) throws IOException {
		// In a development environment, what would the updater update?
		// In development the program is not executed from a location that looks
		// like the installed program location and therefore would not be a
		// location to update. It might be worth "updating" a mock location to
		// prove the logic, but restarting the application based on the initial
		// start parameters would not start the program at the mock location.

		mode = Mode.UPDATE;

		// Stage the updater
		String updaterHome = stageUpdater();
		String updaterJavaExecutablePath = updaterHome + "/bin/" + OperatingSystem.getJavaExecutableName();

		// Linked programs do not have a module path
		String updaterModuleMain = com.avereon.zenna.Program.class.getModule().getName();
		String updaterModuleMainClass = com.avereon.zenna.Program.class.getName();

		Path homeFolder = Paths.get( System.getProperty( "user.home" ) );
		Path logFile = homeFolder.relativize( program.getLogFolder().resolve( "update.%u.log" ) );
		String logFilePath = logFile.toString().replace( File.separator, "/" );

		builder = new ProcessBuilder( ProcessCommands.forModule( updaterJavaExecutablePath, null, updaterModuleMain, updaterModuleMainClass ) );
		builder.directory( new File( updaterHome ) );

		builder.command().add( UpdateFlag.TITLE );
		builder.command().add( "Updating " + program.getCard().getName() );
		builder.command().add( UpdateFlag.LOG_FILE );
		builder.command().add( "%h/" + logFilePath );
		builder.command().add( UpdateFlag.LOG_LEVEL );
		builder.command().add( program.getProgramParameters().get( LogFlag.LOG_LEVEL, "info" ) );
		builder.command().add( UpdateFlag.STDIN );

		log.log( Log.DEBUG,  mode + " command: " + TextUtil.toString( builder.command(), " " ) );

		UpdateCommandBuilder ucb = new UpdateCommandBuilder();
		ucb.add( UpdateTask.ECHO, "Updating " + program.getCard().getName() ).line();

		for( ProductUpdate update : program.getProductManager().getStagedUpdates() ) {
			String key = update.getCard().getProductKey();
			String version = program.getProductManager().getProduct( key ).getCard().getVersion();
			Path backup = program.getDataFolder().resolve( "backup" ).resolve( key + "-" + version );

			String updatePath = update.getSource().toString().replace( File.separator, "/" );
			String targetPath = update.getTarget().toString().replace( File.separator, "/" );
			String backupPath = backup.toString().replace( File.separator, "/" );

			ucb.add( UpdateTask.DELETE, backupPath ).line();
			ucb.add( UpdateTask.MOVE, targetPath, backupPath ).line();
			ucb.add( UpdateTask.UNPACK, updatePath, targetPath ).line();

			if( update.getCard().equals( program.getCard() ) ) {
				String exe = OperatingSystem.isWindows() ? ".exe" : "";
				String cmd = OperatingSystem.isWindows() ? ".bat" : "";
				String javaFile = targetPath + "/bin/java" + exe;
				String javawFile = targetPath + "/bin/javaw" + exe;
				String keytoolFile = targetPath + "/bin/keytool" + exe;
				String scriptFile = targetPath + "/bin/" + program.getCard().getArtifact() + cmd;
				String macScriptFile = targetPath + "/MacOS/" + program.getCard().getArtifact();
				ucb.add( UpdateTask.PERMISSIONS, "777", javaFile, javawFile, keytoolFile, scriptFile, macScriptFile ).line();
			}
		}

		// Add parameters to restart program
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );

		List<String> launchCommands = new ArrayList<>();
		launchCommands.add( System.getProperty( "user.dir" ) );
		launchCommands.addAll( ProcessCommands.forModule(
			OperatingSystem.getJavaExecutablePath(),
			modulePath,
			moduleMain,
			moduleMainClass,
			program.getProgramParameters()
		) );
		launchCommands.addAll( List.of( restartCommands ) );
		ucb.add( UpdateTask.LAUNCH, launchCommands ).line();
		log.log( Log.DEBUG,  ucb.toString() );

		Path updateCommandFile = program.getDataFolder().resolve( "update.commands.txt" );
		Files.writeString( updateCommandFile, ucb.toString() );

		updateCommandsForStdIn = ucb.toString().getBytes( TextUtil.CHARSET );

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
	// TODO This could be converted to a task and the run method can be called at the end
	private String stageUpdater() throws IOException {
		String prefix = program.getCard().getArtifact() + "-updater-";

		// Cleanup from prior updates
		removePriorFolders( prefix );

		// Determine where to put the updater
		Path updaterHomeRoot = FileUtil.createTempFolder( prefix );
		if( program.getProfile() == Profile.DEV ) updaterHomeRoot = Paths.get( System.getProperty( "user.dir" ), "target/" + program.getCard().getArtifact() + "-updater" );

		// Create the updater home folders
		Files.createDirectories( updaterHomeRoot );

		// Copy all the modules needed for the updater
		log.log( Log.DEBUG,  "Copy " + program.getHomeFolder() + " to " + updaterHomeRoot );
		FileUtil.copy( program.getHomeFolder(), updaterHomeRoot );

		// Fix the permissions on the executable
		String ext = OperatingSystem.isWindows() ? ".exe" : "";
		Path bin = updaterHomeRoot.resolve( "bin" ).resolve( OperatingSystem.getJavaExecutableName() + ext );
		if( !bin.toFile().setExecutable( true, true ) ) log.log( Log.WARN,  "Unable to make updater executable: " + bin );

		// NOTE Deleting the updater files when the JVM exits causes the updater to fail to start

		return updaterHomeRoot.toString();
	}

	private void removePriorFolders( String prefix ) throws IOException {
		Files
			.list( FileUtil.getTempFolder() )
			.filter( ( p ) -> p.getFileName().toString().startsWith( prefix ) )
			.forEach( ( p ) -> {
				log.log( Log.INFO,  "Delete prior updater: " + p.getFileName() );
				try {
					FileUtil.delete( p );
				} catch( IOException exception ) {
					log.log( Log.ERROR,  "Unable to cleanup prior updater files", exception );
				}
			} );
	}

	@Override
	public void run() {
		// NOTE The logger does not consistently work here
		// because it is run as the JVM is shutting down
		if( builder == null ) return;
		if( mode == Mode.UPDATE ) program.setUpdateInProgress( true );

		try {
			System.out.println( "Starting " + mode + " process..." );
			// Only discard stdout and stderr
			builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
			Process process = builder.start();
			if( updateCommandsForStdIn != null ) process.getOutputStream().write( updateCommandsForStdIn );
			process.getOutputStream().close();
			System.out.println( mode + " process started!" );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR,  "Error restarting program", throwable );
			throwable.printStackTrace( System.err );
		}
	}

}
