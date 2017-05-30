package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.Resource;
import com.parallelsymmetry.essence.worktool.Tool;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;

public class WorkpaneMain extends Application {

	private static Resource resource = new Resource( URI.create( "" ) );

	@Override
	public void start( Stage stage ) throws Exception {
		Workpane pane = getConfig0();

		StackPane container = new StackPane();
		container.getChildren().add( pane );
		Image image = new Image( getClass().getResourceAsStream( "/wallpaper.jpg" ) );
		BackgroundSize backgroundSize = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true );
		container.setBackground( new Background( new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize ) ) );

		Scene scene = new Scene( container, 640, 360 );
		scene.getStylesheets().add( "style.css" );
		stage.setScene( scene );
		stage.show();
	}

	private static Workpane getConfig0() {
		Workpane pane = new Workpane();
		WorkpaneView view0 = pane.getActiveView();

		pane.split( Side.TOP, 0.25 );
		pane.split( Side.BOTTOM, 0.25 );
		pane.split( Side.LEFT, 0.2 );
		pane.split( Side.RIGHT, 0.2 );

		double sidePercent = 0.15;
		pane.split( view0, Side.LEFT, sidePercent );
		pane.split( view0, Side.RIGHT, 1 / ((1 / sidePercent) - 1) );
		pane.split( view0, Side.TOP, 0.25f );
		pane.split( view0, Side.BOTTOM, 1 / 3f );
		WorkpaneView view1 = pane.split( view0, Side.RIGHT, 0.5f );

		pane.addTool( new MockTool( resource ), view0 );
		pane.addTool( new MockTool( resource ), view1 );

		return pane;
	}

	private static Workpane getConfig1() {
		Workpane pane = new Workpane();
		WorkpaneView view0 = pane.getActiveView();

		WorkpaneView west = pane.split( Side.LEFT, 0.2 );
		pane.split( west, Side.TOP, 0.2 );
		pane.addTool( new MockTool( resource ), view0 );
		pane.addTool( new MockTool( resource ), view0 );

		return pane;
	}

	private static Workpane getConfig2() {
		Workpane pane = new Workpane();
		WorkpaneView view0 = pane.getDefaultView();

		WorkpaneView view1 = pane.split( view0, Side.LEFT );
		view1.getProperties().put( "name", "view1" );

		Tool tool0 = new MockTool( resource );
		Tool tool1 = new MockTool( resource );
		pane.addTool( tool0, view0 );
		pane.addTool( tool1, view1 );
		pane.setActiveView( view1 );
		pane.setActiveTool( tool0 );

		return pane;
	}

}
