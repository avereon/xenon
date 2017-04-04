package com.parallelsymmetry.essence;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class FxUtil {

	public static void centerStage( Stage stage, double width, double height ) {
		stage.setWidth( width );
		stage.setHeight( height );

		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		stage.setX( screenBounds.getMinX() + ((screenBounds.getWidth() - width) / 2) );
		stage.setY( screenBounds.getMinY() + ((screenBounds.getHeight() - height) / 2) );
	}

}
