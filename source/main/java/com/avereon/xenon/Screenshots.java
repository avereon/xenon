package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.asset.type.ProgramAboutType;
import com.avereon.xenon.asset.type.ProgramProductType;
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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

abstract class Screenshots {

	private static final String PROFILE = "screenshots";

	private static final int TIMEOUT = 5000;

	private static final double WIDTH = 800;

	private static final double HEIGHT = 500;

	private int scale;

	private Path screenshots;

	private Program program;

	private Workspace workspace;

	private Workpane workpane;

	private final EventWatcher programWatcher;

	private final FxEventWatcher workpaneWatcher;

	public Screenshots() {
		programWatcher = new EventWatcher( TIMEOUT );
		workpaneWatcher = new FxEventWatcher( TIMEOUT );
	}

	public void generate( int scale ) {
		this.scale = scale;

		try {
			this.screenshots = Paths.get( "target" ).resolve( PROFILE );
			Files.createDirectories( screenshots );
			startup( scale );
			screenshotDefaultWorkarea();
			screenshot( ProgramWelcomeType.URI, "welcome-tool" );
			screenshot( ProgramAboutType.URI, "about-tool" );
			screenshotSettingsPages();
			screenshotProductPages();
			screenshotThemes();
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

	private void screenshotDefaultWorkarea() throws InterruptedException, TimeoutException {
		workspace.screenshot( getPath( "default-workarea" ) );
		reset();
	}

	private void screenshot( URI uri, String path ) throws InterruptedException, TimeoutException {
		program.getAssetManager().openAsset( uri );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workspace.screenshot( getPath( path ) );
		reset();
	}

	private void screenshot( URI uri, String extra, String path ) throws InterruptedException, TimeoutException {
		screenshot( URI.create( uri.toString() + extra ), path );
	}

	private void screenshotSettingsPages() throws InterruptedException, TimeoutException {
		for( String id : program.getSettingsManager().getPageIds() ) {
			screenshot( ProgramSettingsType.URI, "#" + id, "settings/settings-tool-" + id );
		}
	}

	private void screenshotProductPages() throws InterruptedException, TimeoutException {
		screenshot( ProgramProductType.URI, "#installed", "product-tool-installed" );
		//screenshot( ProgramProductType.URI, "#available", "product-tool-available" );
		//screenshot( ProgramProductType.URI, "#updates", "product-tool-updates" );
		screenshot( ProgramProductType.URI, "#sources", "product-tool-sources" );
	}

	private void screenshotThemes() throws InterruptedException, TimeoutException {
		// Set an example tool
		program.getAssetManager().openAsset( ProgramAboutType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		program.getThemeManager().getThemes().stream().map( ThemeMetadata::getId ).forEach( id -> {
			Fx.run( () -> program.getWorkspaceManager().setTheme( id ) );
			workspace.screenshot( getPath( "themes/" + id ) );
		} );
	}

	private Path getPath( String name ) {
		return screenshots.resolve( name + (scale == 1 ? "" : "@" + scale + "x") + ".png" );
	}

	private void startup( double scale ) throws InterruptedException, TimeoutException {
		try {
			Path config = OperatingSystem.getUserProgramDataFolder( "xenon-" + PROFILE, "Xenon-" + PROFILE );
			FileUtil.delete( config );

			program = new Program();
			String[] parameters = new String[]{ ProgramFlag.PROFILE, PROFILE, ProgramFlag.NOUPDATE, ProgramFlag.LOG_LEVEL, ProgramFlag.DEBUG };
			program.setProgramParameters( com.avereon.util.Parameters.parse( parameters ) );
			program.init();
			Platform.startup( () -> {
				try {
					Stage stage = new Stage();

					double actualScale = stage.getRenderScaleX();
					System.out.println( "Screenshots requested-scale=" + scale + " actual-scale=" + actualScale );

					program.start( stage );
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
				program.getWorkspaceManager().getActiveStage().centerOnScreen();
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
			Fx.run( () -> {
				boolean stopping = program.requestExit( true );
				System.out.println( "Screenshots stopping=" + stopping );
				if( stopping ) {
					try {
						program.stop();
					} catch( Exception exception ) {
						exception.printStackTrace();
					}
				}
			} );
			programWatcher.waitForEvent( ProgramEvent.STOPPED, 1000 );
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

}
