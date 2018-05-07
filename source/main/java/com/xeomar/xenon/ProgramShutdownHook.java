package com.xeomar.xenon;

import com.xeomar.util.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This shutdown hook is used when a program restart is requested. When a restart is requested the program registers an instance of this shutdown hook, and stops the program, which triggers this shutdown hook to start the program again.
 *
 * @author soderquistmv
 */
public class ProgramShutdownHook extends Thread {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private volatile Program service;

	private volatile ProcessBuilder builder;

	private volatile byte[] stdInput;

	public ProgramShutdownHook( Program service ) {
		super( "Restart Hook" );
		this.service = service;
	}

	public ProgramShutdownHook configureForRestart( String... commands ) {
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );
		builder = createProcessBuilder( modulePath, moduleMain, moduleMainClass );

		Parameters extraParameters = Parameters.parse( commands );

		// Collect program flags.
		Map<String, List<String>> flags = new HashMap<>();
		for( String name : service.getProgramParameters().getFlags() ) {
			flags.put( name, service.getProgramParameters().getValues( name ) );
		}
		for( String name : extraParameters.getFlags() ) {
			flags.put( name, extraParameters.getValues( name ) );
		}

		// Collect program URIs.
		List<String> uris = new ArrayList<>();
		for( String uri : service.getProgramParameters().getUris() ) {
			if( !uris.contains( uri ) ) uris.add( uri );
		}
		for( String uri : extraParameters.getUris() ) {
			if( !uris.contains( uri ) ) uris.add( uri );
		}

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

		return this;
	}

	public ProgramShutdownHook configureForUpdate( String updaterModulePath, String... commands ) {
		String modulePath = updaterModulePath;
		String moduleMain = com.xeomar.annex.Program.class.getModule().getName();
		String moduleMainClass = com.xeomar.annex.Program.class.getName();
		builder = createProcessBuilder( modulePath, moduleMain, moduleMainClass );

		//Parameters extraParameters = Parameters.parse( commands );

		// NEXT Both of the next steps really should be streamed to the updater
		// TODO Add parameters to update Xenon
		// TODO Add parameters to restart Xenon

		return this;
	}

	private ProcessBuilder createProcessBuilder( String modulePath, String moduleMain, String moduleMainClass ) {
		ProcessBuilder builder = new ProcessBuilder( getRestartExecutablePath( service ) );
		builder.directory( new File( System.getProperty( "user.dir" ) ) );

		// Add the VM parameters to the commands.
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		for( String command : runtimeBean.getInputArguments() ) {
			// These get arbitrarily added by some IDEs
			if( "abort".equals( command ) ) continue;
			if( "exit".equals( command ) ) continue;

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

	@Override
	public void run() {
		if( builder == null ) return;

		log.debug( "Restart command: " + TextUtil.toString( builder.command(), " " ) );

		try {
			Process process = builder.start();
			if( stdInput != null ) {
				process.getOutputStream().write( stdInput );
				process.getOutputStream().flush();
			}
		} catch( IOException exception ) {
			log.error( "Error restarting program", exception );
		}
	}

}
