package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.zerra.javafx.FxEventWatcher;
import com.avereon.zerra.javafx.FxUtil;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.tool.AboutTool;
import com.avereon.xenon.tool.WelcomeTool;
import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import com.avereon.xenon.workspace.Workspace;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

abstract class Screenshots {

	protected static final int TIMEOUT = 2000;

	private static final double WIDTH = 800;

	private static final String PROFILE = "screenshots";

	private static final double HEIGHT = 500;

	private int scale;

	private Path screenshots;

	private Program program;

	private Workspace workspace;

	private Workpane workpane;

	private EventWatcher programWatcher;

	private FxEventWatcher workpaneWatcher;

	public void generate( int scale ) {
		this.scale = scale;
		System.out.println( "Screenshots scale=" + scale );

		try {
			this.screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );

			setup();

			snapshotWelcomeTool();
			snapshotDefaultWorkarea();
			showGuide();
			snapshotAboutTool();
			snapshotSettingsTool();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			shutdown();
		}
	}

	private void snapshotWelcomeTool() throws InterruptedException, TimeoutException {
		//		program.getAssetManager().openAsset( ProgramWelcomeType.URI );
		//		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.snapshot( getPath( "welcome-tool" ) );
		Platform.runLater( () -> workpane.closeTools( workpane.getTools( WelcomeTool.class ) ) );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private void snapshotDefaultWorkarea() {
		workspace.snapshot( getPath( "default-workarea" ) );
	}

	private void showGuide() throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
	}

	private void snapshotAboutTool() throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( ProgramAboutType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.snapshot( getPath( "about-tool" ) );
		Platform.runLater( () -> workpane.closeTools( workpane.getTools( AboutTool.class ) ) );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private void snapshotSettingsTool() throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( ProgramSettingsType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.snapshot( getPath( "settings-tool" ) );
		Platform.runLater( () -> workpane.closeTools( workpane.getTools( SettingsTool.class ) ) );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private Path getPath( String name ) {
		return screenshots.resolve( name + (scale == 1 ? "" : "@" + scale + "x") + ".png" );
	}

	private void setup() throws InterruptedException {
		if( FxUtil.isFxRunning() ) FxUtil.fxWait( FxEventWatcher.DEFAULT_WAIT_TIMEOUT );
		startup();
		workspace = program.getWorkspaceManager().getActiveWorkspace();
		workpane = workspace.getActiveWorkarea().getWorkpane();
		workpaneWatcher = new FxEventWatcher();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		FxUtil.fxWait( FxEventWatcher.DEFAULT_WAIT_TIMEOUT );
	}

	private void startup() {
		try {
			Path config = OperatingSystem.getUserProgramDataFolder( "xenon-" + PROFILE, "Xenon-" + PROFILE );
			FileUtil.delete( config );

			program = new Program();
			String[] parameters = new String[]{ ProgramFlag.PROFILE, PROFILE, ProgramFlag.NOUPDATE, ProgramFlag.LOG_LEVEL, ProgramFlag.DEBUG };
			program.setProgramParameters( com.avereon.util.Parameters.parse( parameters ) );
			program.init();
			Platform.startup( () -> {
				try {
					program.start( new Stage() );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} );
			program.register( ProgramEvent.ANY, programWatcher = new EventWatcher( 5000 ) );
			programWatcher.waitForEvent( ProgramEvent.STARTED );
			Platform.runLater( () -> {
				//program.getWorkspaceManager().getActiveStage().setX( 0 );
				//program.getWorkspaceManager().getActiveStage().setY( 0 );
				program.getWorkspaceManager().getActiveStage().setWidth( scale * WIDTH );
				program.getWorkspaceManager().getActiveStage().setHeight( scale * HEIGHT );
			} );
			FxUtil.fxWait( FxEventWatcher.DEFAULT_WAIT_TIMEOUT );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private void shutdown() {
		try {
			boolean stopping = program.requestExit( true );
			System.out.println( "Screenshots stopping=" + stopping );
			if( stopping ) program.stop();
			programWatcher.waitForEvent( ProgramEvent.STOPPED, 1000 );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

}
