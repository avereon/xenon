package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.event.ProgramStartedEvent;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import com.avereon.xenon.tool.about.AboutTool;
import com.avereon.xenon.tool.settings.SettingsTool;
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
import java.util.concurrent.TimeoutException;

public class ScreenShots implements Runnable {

	private static String PROFILE = "screenshots";

	private Program program;

	private ProgramWatcher programWatcher;

	private Workspace workspace;

	private Workpane workpane;

	private WorkpaneWatcher workpaneWatcher;

	public static void main( String[] commands ) {
		new ScreenShots().run();
	}

	public void run() {
		try {
			Path screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );

			setup();
			snapshotWelcomeTool( screenshots );
			snapshotDefaultWorkarea( screenshots );
			snapshotAboutTool( screenshots );
			snapshotSettingsTool( screenshots );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			Platform.runLater( this::shutdown );
		}
	}

	private void snapshotSettingsTool( Path screenshots ) throws InterruptedException, TimeoutException {
		program.getResourceManager().open( ProgramSettingsType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workspace.snapshot( screenshots.resolve( "settings-tool.png" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( SettingsTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
	}

	private void snapshotAboutTool( Path screenshots ) throws InterruptedException, TimeoutException {
		program.getResourceManager().open( ProgramAboutType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workspace.snapshot( screenshots.resolve( "about-tool.png" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( AboutTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
	}

	private void startup() {
		try {
			Path config = OperatingSystem.getUserProgramDataFolder( "xenon-" + PROFILE, "Xenon-" + PROFILE );
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
			program.addEventListener( programWatcher = new ProgramWatcher() );
			programWatcher.waitForEvent( ProgramStartedEvent.class );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private void setup() {
		startup();
		workspace = program.getWorkspaceManager().getActiveWorkspace();
		workpane = workspace.getActiveWorkarea().getWorkpane();
		workpaneWatcher = new WorkpaneWatcher();
		workpane.addWorkpaneListener( workpaneWatcher );
	}

	private void snapshotWelcomeTool( Path screenshots ) throws InterruptedException, TimeoutException {
		workspace.snapshot( screenshots.resolve( "welcome-tool.png" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( WelcomeTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
	}

	private void snapshotDefaultWorkarea( Path screenshots ) {
		workspace.snapshot( screenshots.resolve( "default-workarea.png" ) );
	}

	private void shutdown() {
		program.requestExit( true );
	}

}
