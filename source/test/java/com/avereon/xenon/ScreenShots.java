package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.event.ProgramStartedEvent;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.tool.welcome.WelcomeTool;
import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneEvent;
import com.avereon.xenon.workarea.WorkpaneWatcher;
import com.avereon.xenon.workspace.Workspace;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreenShots implements Runnable {

	private static String PROFILE = "screenshots";

	private Program program;

	private ProgramWatcher programWatcher;

	public static void main( String[] commands ) {
		new ScreenShots().run();
	}

	public void run() {
		try {
			Path screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );

			// Wait for startup
			startup();
			program.addEventListener( programWatcher = new ProgramWatcher() );
			programWatcher.waitForEvent( ProgramStartedEvent.class );

			Workspace workspace = program.getWorkspaceManager().getActiveWorkspace();
			Workpane workpane = workspace.getActiveWorkarea().getWorkpane();
			WorkpaneWatcher workpaneWatcher = new WorkpaneWatcher();
			workpane.addWorkpaneListener( workpaneWatcher );

			// Snapshot the welcome tool
			workspace.snapshot( screenshots.resolve( "welcome-tool.png" ) );

			// Snapshot the default workarea
			Platform.runLater( () -> workpane.closeTool( workpane.getTools( WelcomeTool.class ).iterator().next() ) );
			workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
			workspace.snapshot( screenshots.resolve( "default-workarea.png" ) );

			// Snapshot the about tool
			program.getResourceManager().open( ProgramAboutType.URI );
			workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
			workspace.snapshot( screenshots.resolve( "about-tool.png" ) );

		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			Platform.runLater( this::shutdown );
		}
	}

	private void startup() {
		try {
			Path config = OperatingSystem.getUserProgramDataFolder( "xenon-" + PROFILE, "Xenon-" + PROFILE);
			FileUtil.delete( config );

			program = new Program();
			program.setProgramParameters( com.avereon.util.Parameters.parse( ProgramFlag.PROFILE, PROFILE ) );
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
