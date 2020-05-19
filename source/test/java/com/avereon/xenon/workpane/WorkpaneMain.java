package com.avereon.xenon.workpane;

import com.avereon.xenon.asset.Asset;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class WorkpaneMain extends Application {

	private static final Asset asset = Asset.NONE;

	@Override
	public void start( Stage stage ) {
		Workpane pane = getConfig0();

		StackPane container = new StackPane();
		container.getChildren().add( pane );
		Image image = new Image( getClass().getResourceAsStream( "/wallpaper.jpg" ) );
		BackgroundSize backgroundSize = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true );
		container.setBackground( new Background( new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize ) ) );

		Scene scene = new Scene( container, 640, 360 );
		scene.getStylesheets().add( "xenon.css" );
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

		pane.addTool( new MockTool( asset ), view0 );
		pane.addTool( new MockTool( asset ), view1 );

		return pane;
	}

}
