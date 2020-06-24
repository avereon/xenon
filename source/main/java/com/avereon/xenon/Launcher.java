package com.avereon.xenon;

import com.avereon.util.JvmSureStop;
import com.avereon.zenna.ElevatedFlag;
import javafx.application.Platform;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Launcher {

	public static void main( String[] commands ) {
		System.out.println( "Starting with Launcher" );
		Map<Thread, StackTraceElement[]> threadStacks = Thread.getAllStackTraces();
		List<Thread> threads = threadStacks.keySet().stream().filter( t -> !t.isDaemon() ).collect( Collectors.toList());
		for( Thread t : threads ) {
			if( "JavaFX Application Thread".equals( t.getName())) throw new RuntimeException( "JavaFX already alive!");
		}

		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( commands );
		if( parameters.isSet( ElevatedFlag.CALLBACK_SECRET ) ) {
			// FIXME The logging does not configure due to an NPE
			new com.avereon.zenna.Program().configAndStart( commands );
			//		} else if( parameters.isSet( ProgramFlag.UPDATE ) ) {
			//			if( startup ) {
			//				updateProgram( parameters );
			//			} else {
			//				log.log( WARNING, "Cannot run an update from a peer!" );
			//			}
			//			return false;
			//		} else if( parameters.isSet( ElevatedFlag.CALLBACK_SECRET ) ) {
			//			if( startup ) {
			//				updateProgram( parameters );
			//			} else {
			//				log.log( WARNING, "Cannot run an elevated update from a peer!" );
			//			}
			//			return false;
		} else if( parameters.isSet( ProgramFlag.UPDATE ) ) {
			Program program = new Program();
			try {
				// Configure launcher name, data folder
				program.config();
				program.updateProgram( parameters );
			} catch( Exception exception ) {
				exception.printStackTrace();
			}
			Platform.exit();
		} else {
			Program.launch( commands );
		}

		new JvmSureStop().start();
	}

}
