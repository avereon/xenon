package com.xeomar.xenon.workspace;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.util.Colors;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkspaceBackground extends Pane {

	private Pane backPane;

	private Pane imagePane;

	private Pane tintPane;

	private String style = "fill";

	private String align = "center";

	public WorkspaceBackground() {
		backPane = new Pane();
		imagePane = new Pane();
		tintPane = new Pane();

		imagePane.setVisible( false );
		tintPane.setVisible( false );

		backPane.prefWidthProperty().bind( this.widthProperty() );
		backPane.prefHeightProperty().bind( this.heightProperty() );
		tintPane.prefWidthProperty().bind( this.widthProperty() );
		tintPane.prefHeightProperty().bind( this.heightProperty() );

		getChildren().addAll( backPane, imagePane, tintPane );

		Rectangle clipRectangle = new Rectangle();
		this.setClip( clipRectangle );
		this.layoutBoundsProperty().addListener( ( observable, oldValue, newValue ) -> {
			clipRectangle.setWidth( newValue.getWidth() );
			clipRectangle.setHeight( newValue.getHeight() );
		} );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		if( imagePane.getBackground() == null ) return;
		Image image = imagePane.getBackground().getImages().get( 0 ).getImage();

		double spaceWidth = getWidth();
		double spaceHeight = getHeight();
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();

		boolean tallSpace = spaceHeight > spaceWidth;

		switch( style ) {
			case "fill": {
				//				if( tallSpace ) {
				//					imagePane.setFitHeight( spaceWidth );
				//				} else {
				//					imagePane.setFitWidth( spaceHeight );
				//				}
				break;
			}
			case "fit": {
				//				imagePane.fitHeightProperty().bind( this.heightProperty() );
				//				imagePane.fitWidthProperty().bind( this.widthProperty() );
				//				imagePane.setPreserveRatio( true );
				break;
			}
			case "stretch": {
				imagePane.setPrefWidth( spaceWidth );
				imagePane.setPrefHeight( spaceHeight );
				break;
			}
			case "tile": {
				// Not implemented
				break;
			}
			case "anchor": {
				// No scaling should be applied, just anchor the image according to the align parameter
				imagePane.setPrefWidth( imageWidth );
				imagePane.setPrefHeight( imageHeight );
				break;
			}
		}

		// Start with the image centered.
		double x = (spaceWidth - imageWidth) / 2;
		double y = (spaceHeight - imageHeight) / 2;

		switch( align ) {
			case "northwest": {
				x = 0;
				y = 0;
				break;
			}
			case "north": {
				y = 0;
				break;
			}
			case "northeast": {
				x = spaceWidth - imageWidth;
				y = 0;
				break;
			}
			case "west": {
				x = 0;
				break;
			}
			case "center": {
				break;
			}
			case "east": {
				x = spaceWidth - imageWidth;
				break;
			}
			case "southwest": {
				x = 0;
				y = spaceHeight - imageHeight;
				break;
			}
			case "south": {
				y = spaceHeight - imageHeight;
				break;
			}
			case "southeast": {
				x = spaceWidth - imageWidth;
				y = spaceHeight - imageHeight;
				break;
			}
		}

		//System.out.println( "x=" + x + "  y=" + y );

		if( !"stretch".equals( style ) ) {
			imagePane.setLayoutX( x );
			imagePane.setLayoutY( y );
		}
	}

	void updateBackgroundFromSettings( Settings settings ) {
		// Back layer
		boolean backDirection = "0".equals( settings.get( "workspace-scenery-back-direction", "0" ) );
		Color backColor1 = Colors.web( settings.get( "workspace-scenery-back-color1", "#80a0c0ff" ) );
		Color backColor2 = Colors.web( settings.get( "workspace-scenery-back-color2", "#ffffffff" ) );
		LinearGradient backFill;
		Stop[] stops = new Stop[]{ new Stop( 0, backColor1 ), new Stop( 1, backColor2 ) };
		if( backDirection ) {
			backFill = new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops );
		} else {
			backFill = new LinearGradient( 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops );
		}

		// Image layer
		Image image = null;
		boolean imageEnabled = Boolean.parseBoolean( settings.get( "workspace-scenery-image-enabled", "false" ) );
		String imageFile = settings.get( "workspace-scenery-image-file", (String)null );
		Path imagePath = imageFile == null ? null : Paths.get( imageFile );
		if( imageEnabled && imagePath != null && Files.exists( imagePath ) ) image = new Image( imagePath.toUri().toString() );

		style = settings.get( "workspace-scenery-image-style", "fill" );
		align = settings.get( "workspace-scenery-image-align", "center" );

		// Tint layer
		boolean tintEnabled = Boolean.parseBoolean( settings.get( "workspace-scenery-tint-enabled", "false" ) );
		boolean tintDirection = "0".equals( settings.get( "workspace-scenery-tint-direction", "0" ) );
		Color tintColor1 = Colors.web( settings.get( "workspace-scenery-tint-color1", "#ffffff80" ) );
		Color tintColor2 = Colors.web( settings.get( "workspace-scenery-tint-color2", "#ffffff80" ) );
		LinearGradient tintFill;
		Stop[] tintStops = new Stop[]{ new Stop( 0, tintColor1 ), new Stop( 1, tintColor2 ) };
		if( tintDirection ) {
			tintFill = new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE, tintStops );
		} else {
			tintFill = new LinearGradient( 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, tintStops );
		}

		backPane.setBackground( new Background( new BackgroundFill( backFill, null, null ) ) );

		// NEXT The BackgroundImage class may have enough parameters to do what I want
		if( image == null ) {
			imagePane.setBackground( null );
		} else {
			imagePane.setBackground( new Background( new BackgroundImage( image, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, null, null ) ) );
		}
		imagePane.setVisible( imageEnabled );

		tintPane.setBackground( new Background( new BackgroundFill( tintFill, null, null ) ) );
		tintPane.setVisible( tintEnabled );

		requestLayout();
	}

}
