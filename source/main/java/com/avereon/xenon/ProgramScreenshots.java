package com.avereon.xenon;

import com.avereon.event.EventWatcher;
import com.avereon.util.FileUtil;
import com.avereon.util.OperatingSystem;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import com.avereon.xenon.workspace.Workspace;
import com.avereon.zerra.event.FxEventWatcher;
import com.avereon.zerra.javafx.Fx;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class ProgramScreenshots {

	protected static final String MODE = "screenshots";

	private static final int TIMEOUT = 5000;

	private static final double WIDTH = 800;

	private static final double HEIGHT = 500;

	@Getter
	private Xenon program;

	@Getter
	private Workspace workspace;

	@Getter
	private Workpane workpane;

	@Getter
	private final EventWatcher programWatcher;

	@Getter
	private final FxEventWatcher workpaneWatcher;

	private double scale;

	private Path screenshotPath;

	public ProgramScreenshots() {
		programWatcher = new EventWatcher( TIMEOUT );
		workpaneWatcher = new FxEventWatcher( TIMEOUT );
	}

	public final void generate( String[] args ) {
		this.scale = 1;
		if( args.length > 0 ) {
			try {
				this.scale = Double.parseDouble( args[ 0 ] );
			} catch( NumberFormatException exception ) {
				exception.printStackTrace( System.err );
			}
		}

		System.setProperty( "glass.gtk.uiScale", String.valueOf( scale ) );

		try {
			this.screenshotPath = Paths.get( "target" ).resolve( MODE );
			Files.createDirectories( screenshotPath );
			startup( scale );

			generateScreenshots();
		} catch( ExecutionException | IOException | TimeoutException | InterruptedException exception ) {
			exception.printStackTrace( System.err );
		} finally {
			shutdown();
		}
	}

	protected double getRenderScale() {
		return scale;
	}

	protected String getExecutionMode() {
		return MODE;
	}

	protected String getLogLevel() {
		return XenonFlag.INFO;
	}

	protected List<String> getProgramParameters() {
		return List.of( XenonFlag.NO_UPDATES, XenonFlag.MODE, getExecutionMode(), XenonFlag.LOG_LEVEL, getLogLevel() );
	}

	protected abstract void generateScreenshots() throws InterruptedException, TimeoutException, ExecutionException;

	protected void reset() throws InterruptedException, TimeoutException {
		Collection<Tool> tools = workpane.getTools();
		Fx.run( () -> workpane.closeTools( tools ) );
		for( int index = 0; index < tools.size(); index++ ) {
			workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		}
	}

	protected void openAsset( URI uri ) throws InterruptedException, TimeoutException, ExecutionException {
		program.getAssetManager().openAsset( uri );
		workpaneWatcher.waitForEvent( ProgramToolEvent.READY );
		getProgram().getTaskManager().waitFor( TIMEOUT );
	}

	protected void screenshot( String output ) throws InterruptedException, TimeoutException {
		doScreenshotAndReset( output );
	}

	protected void screenshot( URI uri, String output ) throws InterruptedException, TimeoutException, ExecutionException {
		openAsset( uri );
		doScreenshotAndReset( output );
	}

	protected void screenshot( URI uri, String fragment, String output ) throws InterruptedException, TimeoutException, ExecutionException {
		screenshot( URI.create( uri.toString() + "#" + fragment ), output );
	}

	protected void screenshotNoReset( String output ) {
		doScreenshot( output );
	}

	private void doScreenshot( String path ) {
		workspace.screenshot( getPath( path ) );
	}

	private void doScreenshotAndReset( String path ) throws InterruptedException, TimeoutException {
		doScreenshot( path );
		reset();
	}

	static String formatScale( double scale ) {
		String scaleText;
		if( scale == 1.0 ) {
			scaleText = "";
		} else if( scale % 1.0 == 0.0 ) {
			// If the scale is an integer, don't include the decimal point
			scaleText = "@" + (int)scale + "x";
		} else {
			scaleText = "@" + scale + "x";
		}
		return scaleText;
	}

	private Path getPath( String name ) {
		return screenshotPath.resolve( name + formatScale( scale ) + ".png" );
	}

	private void startup( double scale ) throws InterruptedException, TimeoutException {
		try {
			Path config = OperatingSystem.getUserProgramDataFolder( "xenon-" + MODE, "Xenon-" + MODE );
			FileUtil.delete( config );

			program = new Xenon();
			program.setProgramParameters( com.avereon.util.Parameters.parse( getProgramParameters() ) );
			program.init();
			Platform.startup( () -> {
				try {
					program.start( new Stage() );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} );
			program.register( ProgramEvent.ANY, programWatcher );
			// Startup can take a while so give it more time than usual
			programWatcher.waitForEvent( ProgramEvent.STARTED, programWatcher.getTimeout() * 2 );
			Fx.run( () -> {
				program.getWorkspaceManager().getActiveStage().setWidth( WIDTH );
				program.getWorkspaceManager().getActiveStage().setHeight( HEIGHT );
				program.getWorkspaceManager().getActiveStage().centerOnScreen();
			} );
			Fx.waitForWithExceptions( programWatcher.getTimeout() );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}

		String uiScale = System.getProperty( "glass.gtk.uiScale" );
		double actualScale = program.getWorkspaceManager().getActiveStage().getRenderScaleX();
		System.out.println( "Screenshots req-scale=" + scale + " uiScale=" + uiScale + " scale=" + actualScale );

		workspace = program.getWorkspaceManager().getActiveWorkspace();
		workpane = workspace.getActiveWorkarea();
		workpane.addEventHandler( WorkpaneEvent.ANY, workpaneWatcher );
		Fx.waitForWithExceptions( workpaneWatcher.getTimeout() );
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
						exception.printStackTrace( System.err );
					}
				}
			} );
			programWatcher.waitForEvent( ProgramEvent.STOPPED, programWatcher.getTimeout() );
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		}
	}

}
