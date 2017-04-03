package com.parallelsymmetry.essence;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Canvas {

	private Stage stage;

	public SplashScreen( String title ) {
		super( 320, 180 );
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();

		stage = new Stage( StageStyle.UTILITY );
		stage.setTitle( title );
		stage.setResizable( false );
		stage.setAlwaysOnTop( true );
		stage.setScene( new Scene( new VBox( this ) ) );
		stage.setX( screenBounds.getMinX() + ((screenBounds.getWidth() - this.getWidth()) / 2) );
		stage.setY( screenBounds.getMinY() + ((screenBounds.getHeight() - this.getHeight()) / 2) );
	}

	public SplashScreen show() {
		stage.show();
		return this;
	}

	public SplashScreen hide() {
		stage.hide();
		return this;
	}

	public void update() {}

}
