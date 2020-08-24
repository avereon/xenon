package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.javafx.Fx;
import com.avereon.zerra.javafx.FxEventWatcher;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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

	private final EventWatcher programWatcher;

	private final FxEventWatcher workpaneWatcher;

	public Screenshots() {
		programWatcher = new EventWatcher();
		workpaneWatcher = new FxEventWatcher();
	}

	public void generate( int scale ) {
		this.scale = scale;
		System.out.println( "Screenshots scale=" + scale );

		try {
			this.screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );
			startup();
			snapshotDefaultWorkarea();
			reset();
			snapshotWelcomeTool();
			reset();
			snapshotAboutTool();
			reset();
			snapshotSettingsTool();
			reset();
			// TODO The product management tool
			snapshotThemes();
			reset();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		} finally {
			shutdown();
		}
	}

	private void reset() throws InterruptedException, TimeoutException {
		Collection<Tool> tools = workpane.getTools();
		Fx.run( () -> workpane.closeTools( tools ) );
		for( int index = 0; index < tools.size(); index++ ) {
			workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		}
	}

	private void snapshotDefaultWorkarea() {
		workspace.snapshot( getPath( "default-workarea" ) );
	}

	private void snapshotWelcomeTool() throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( ProgramWelcomeType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.snapshot( getPath( "welcome-tool" ) );
		//Fx.run( () -> workpane.closeTools( workpane.getTools( WelcomeTool.class ) ) );
		//workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private void snapshotAboutTool() throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( ProgramAboutType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.snapshot( getPath( "about-tool" ) );
		//Fx.run( () -> workpane.closeTools( workpane.getTools( AboutTool.class ) ) );
		//workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private void snapshotSettingsTool() throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( ProgramSettingsType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.snapshot( getPath( "settings-tool" ) );
		//Fx.run( () -> workpane.closeTools( workpane.getTools( SettingsTool.class ) ) );
		//workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
	}

	private void snapshotThemes() throws InterruptedException, TimeoutException {
		// Set an example tool
		program.getAssetManager().openAsset( ProgramAboutType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		program.getThemeManager().getThemes().stream().map( ThemeMetadata::getId ).forEach( id -> {
			Fx.run( () -> program.getWorkspaceManager().setTheme( id ) );
			workspace.snapshot( getPath( "themes/" + id ) );
		} );
	}

	private Path getPath( String name ) {
		return screenshots.resolve( name + (scale == 1 ? "" : "@" + scale + "x") + ".png" );
	}

	private void startup() throws InterruptedException, TimeoutException {
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
			program.register( ProgramEvent.ANY, programWatcher );
			// NOTE Startup can take a while so give it more time than usual
			programWatcher.waitForEvent( ProgramEvent.STARTED, programWatcher.getTimeout() * 2 );
			Fx.run( () -> {
				program.getWorkspaceManager().getActiveStage().setWidth( scale * WIDTH );
				program.getWorkspaceManager().getActiveStage().setHeight( scale * HEIGHT );
			} );
			Fx.waitForWithInterrupt( programWatcher.getTimeout() );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}

		workspace = program.getWorkspaceManager().getActiveWorkspace();
		workpane = workspace.getActiveWorkarea().getWorkpane();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		Fx.waitForWithInterrupt( workpaneWatcher.getTimeout() );
		reset();
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
