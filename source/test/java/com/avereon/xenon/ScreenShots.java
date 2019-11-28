package com.avereon.xenon;

import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.event.ProgramStartedEvent;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import com.avereon.xenon.tool.about.AboutTool;
import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.tool.welcome.WelcomeTool;
import com.avereon.xenon.util.FxUtil;
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

public abstract class ScreenShots implements Runnable {

	private static String PROFILE = "screenshots";

	private int scale;

	private Path screenshots;

	private Program program;

	private ProgramWatcher programWatcher;

	private Workspace workspace;

	private Workpane workpane;

	private WorkpaneWatcher workpaneWatcher;

	ScreenShots( int scale ) {
		this.scale = scale;
	}

	public void run() {
		try {
			this.screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );

			setup();

			snapshotWelcomeTool();
			snapshotDefaultWorkarea();
			snapshotAboutTool();
			snapshotSettingsTool();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			Platform.runLater( this::shutdown );
		}
	}

	private void setup() throws InterruptedException {
		if( FxUtil.isFxRunning() ) FxUtil.fxWait( 2000 );
		startup();
		workspace = program.getWorkspaceManager().getActiveWorkspace();
		workpane = workspace.getActiveWorkarea().getWorkpane();
		workpaneWatcher = new WorkpaneWatcher();
		workpane.addWorkpaneListener( workpaneWatcher );
		FxUtil.fxWait( 2000 );
	}

	private void snapshotWelcomeTool() throws InterruptedException, TimeoutException {
		workspace.snapshot( getPath( "welcome-tool" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( WelcomeTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
	}

	private void snapshotDefaultWorkarea() {
		workspace.snapshot( getPath( "default-workarea" ) );
	}

	private void snapshotSettingsTool() throws InterruptedException, TimeoutException {
		program.getResourceManager().open( ProgramSettingsType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workspace.snapshot( getPath( "settings-tool" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( SettingsTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
	}

	private void snapshotAboutTool() throws InterruptedException, TimeoutException {
		program.getResourceManager().open( ProgramAboutType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workspace.snapshot( getPath( "about-tool" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( AboutTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
	}

	private Path getPath( String name ) {
		return screenshots.resolve( name + (scale == 1 ? "" : "@" + scale + "x") + ".png" );
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
			Platform.runLater( () -> {
				program.getWorkspaceManager().getActiveStage().setX( 0 );
				program.getWorkspaceManager().getActiveStage().setY( 0 );
				program.getWorkspaceManager().getActiveStage().setWidth( scale * UiFactory.DEFAULT_WIDTH );
				program.getWorkspaceManager().getActiveStage().setHeight( scale * UiFactory.DEFAULT_HEIGHT );
			} );
			FxUtil.fxWait( 2000 );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private void shutdown() {
		program.requestExit( true );
	}

}
