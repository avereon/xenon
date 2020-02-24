package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.venza.javafx.FxEventWatcher;
import com.avereon.venza.javafx.FxUtil;
import com.avereon.xenon.tool.WelcomeTool;
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

public class Screenshots {

	private static String PROFILE = "screenshots";

	private int scale;

	private Path screenshots;

	private Program program;

	private Workspace workspace;

	private Workpane workpane;

	private FxEventWatcher workpaneWatcher;

	Screenshots( int scale ) {
		this.scale = scale;
	}

	public static void main( String[] args ) {
		new Screenshots( 2 ).run();
		new Screenshots( 1 ).run();
		Platform.exit();
	}

	public void run() {
		System.out.println( "Screenshots scale=" + scale );
		try {
			this.screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );

			setup();

			snapshotWelcomeTool();
			//			snapshotDefaultWorkarea();
			//			snapshotAboutTool();
			//			snapshotSettingsTool();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			Platform.runLater( this::shutdown );
		}
	}

	private void snapshotWelcomeTool() throws InterruptedException, TimeoutException {
		workspace.snapshot( getPath( "welcome-tool" ) );
		Platform.runLater( () -> workpane.closeTool( workpane.getTools( WelcomeTool.class ).iterator().next() ) );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private Path getPath( String name ) {
		return screenshots.resolve( name + (scale == 1 ? "" : "@" + scale + "x") + ".png" );
	}

	private void setup() throws InterruptedException {
		if( FxUtil.isFxRunning() ) FxUtil.fxWait( 2000 );
		startup();
		workspace = program.getWorkspaceManager().getActiveWorkspace();
		workpane = workspace.getActiveWorkarea().getWorkpane();
		workpaneWatcher = new FxEventWatcher();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		FxUtil.fxWait( 2000 );
	}

	private void startup() {
		try {
			Path config = OperatingSystem.getUserProgramDataFolder( "xenon-" + PROFILE, "Xenon-" + PROFILE );
			FileUtil.delete( config );

			program = new Program();
			String[] parameters = new String[]{ ProgramFlag.PROFILE, PROFILE, ProgramFlag.NOUPDATE };
			program.setProgramParameters( com.avereon.util.Parameters.parse( parameters ) );
			program.init();
			Platform.startup( () -> {
				try {
					program.start( new Stage() );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} );
			EventWatcher programWatcher;
			program.getEventBus().register( ProgramEvent.ANY, programWatcher = new EventWatcher() );
			programWatcher.waitForEvent( ProgramEvent.STARTED );
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
