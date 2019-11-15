package com.avereon.xenon;

import com.avereon.xenon.event.ProgramStartedEvent;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ScreenShots implements Runnable {

	private Program program;

	private ProgramWatcher programWatcher;

	public static void main( String[] commands ) {
		new ScreenShots().run();
	}

	public void run() {
		try {
			startup();

			// Wait for startup
			program.addEventListener( programWatcher = new ProgramWatcher() );
			programWatcher.waitForEvent( ProgramStartedEvent.class );

			Thread.sleep(1000);
			// TODO Time to take snap shots

		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			Platform.runLater( this::shutdown );
		}
	}

	private void startup() {
		program = new Program();

		com.avereon.util.Parameters parameters = com.avereon.util.Parameters.parse( ProgramFlag.PROFILE, "screenshots" );

		try {
			program.setProgramParameters( parameters );
			program.init();
			Platform.startup( () -> {
				try {
					program.start( new Stage() );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private void shutdown() {
		program.requestExit( true );
	}

}
