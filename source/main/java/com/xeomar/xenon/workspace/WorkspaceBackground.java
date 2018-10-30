package com.xeomar.xenon.workspace;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.util.Colors;
import com.xeomar.xenon.util.FxUtil;
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

	private Image image;

	private String style;

	private String align;

	private double priorSpaceRatio;

	public WorkspaceBackground() {
		backPane = new Pane();
		imagePane = new Pane();
		tintPane = new Pane();

		imagePane.setVisible( false );
		tintPane.setVisible( false );

		backPane.prefWidthProperty().bind( this.widthProperty() );
		backPane.prefHeightProperty().bind( this.heightProperty() );
		imagePane.prefWidthProperty().bind( this.widthProperty() );
		imagePane.prefHeightProperty().bind( this.heightProperty() );
		tintPane.prefWidthProperty().bind( this.widthProperty() );
		tintPane.prefHeightProperty().bind( this.heightProperty() );

		getChildren().addAll( backPane, imagePane, tintPane );

		Rectangle clip = new Rectangle();
		this.setClip( clip );
		this.layoutBoundsProperty().addListener( ( observable, oldBounds, newBounds ) -> {
			clip.setWidth( newBounds.getWidth() );
			clip.setHeight( newBounds.getHeight() );
			useFillBackgroundWorkaround( false );
		} );
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
		image = null;
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

		configureImagePane();
		imagePane.setVisible( imageEnabled );

		tintPane.setBackground( new Background( new BackgroundFill( tintFill, null, null ) ) );
		tintPane.setVisible( tintEnabled );
	}

	private void configureImagePane() {
		if( image == null ) {
			imagePane.setBackground( null );
			return;
		}

		BackgroundImage background = null;
		BackgroundPosition position = FxUtil.parseBackgroundPosition( align );

		switch( style ) {
			case "fill": {
				// JavaFX cover
				useFillBackgroundWorkaround( true );
				break;
			}
			case "fit": {
				// JavaFX contain
				BackgroundSize size = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false );
				background = new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, size );
				break;
			}
			case "stretch": {
				BackgroundSize size = new BackgroundSize( 1, 1, true, true, false, false );
				background = new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, size );
				break;
			}
			case "tile": {
				BackgroundSize size = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, false );
				background = new BackgroundImage( image, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, position, size );
				break;
			}
			case "anchor": {
				BackgroundSize size = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, false );
				background = new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, size );
				break;
			}
		}

		if( background != null ) imagePane.setBackground( new Background( background ) );
	}

	/**
	 * This workaround is in place to handle updating the fill style because the
	 * BackgroundSize cover flag does not respect BackgroundPosition yet.
	 */
	private void useFillBackgroundWorkaround( boolean force ) {
		if( !"fill".equals( style ) ) return;

		double spaceWidth = getWidth();
		double spaceHeight = getHeight();
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();

		double spaceRatio = spaceWidth / spaceHeight;
		double imageRatio = imageWidth / imageHeight;
		if( spaceWidth == 0 || spaceHeight == 0 ) spaceRatio = Double.POSITIVE_INFINITY;

		boolean swap = (priorSpaceRatio > imageRatio && spaceRatio < imageRatio) || (priorSpaceRatio < imageRatio && spaceRatio > imageRatio);
		boolean change = force || swap;

		BackgroundPosition position = FxUtil.parseBackgroundPosition( align );
		if( change && spaceRatio < imageRatio ) {
			// Switch to tall settings
			BackgroundSize size = new BackgroundSize( BackgroundSize.AUTO, 1, false, true, false, false );
			BackgroundImage newBackground = new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, size );
			imagePane.setBackground( new Background( newBackground ) );
		} else if( change && spaceRatio >= imageRatio ) {
			// Switch to wide settings
			BackgroundSize size = new BackgroundSize( 1, BackgroundSize.AUTO, true, false, false, false );
			BackgroundImage newBackground = new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, size );
			imagePane.setBackground( new Background( newBackground ) );
		}

		priorSpaceRatio = spaceRatio;
	}

}
