package com.avereon.xenon.workspace;

import com.avereon.settings.Settings;
import com.avereon.util.FileUtil;
import com.avereon.zarra.color.Colors;
import com.avereon.zarra.javafx.FxUtil;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import lombok.CustomLog;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@CustomLog
public class WorkspaceBackground extends StackPane {

	private final Pane backPane;

	private final Pane imagePane;

	private final Pane tintPane;

	private final Pane themeTintPane;

	private final Pane customTintPane;

	private Image image;

	private String style;

	private String align;

	private double priorSpaceRatio;

	public WorkspaceBackground() {
		backPane = new Pane();
		imagePane = new Pane();
		tintPane = new StackPane();
		themeTintPane = new Pane();
		customTintPane = new Pane();

		themeTintPane.getStyleClass().add( "workspace-tint" );

		imagePane.setVisible( false );
		tintPane.setVisible( false );
		themeTintPane.setVisible( false );
		customTintPane.setVisible( false );

		tintPane.getChildren().addAll( themeTintPane, customTintPane );
		getChildren().addAll( backPane, imagePane, tintPane );
	}

	public static List<Path> listImageFiles( Path folder ) throws IOException {
		List<Path> result = new ArrayList<>();
		try( DirectoryStream<Path> stream = Files.newDirectoryStream( folder, "*.{gif,GIF,jpg,JPG,jpeg,JPEG,png,PNG}" ) ) {
			for( Path entry : stream ) {
				result.add( entry );
			}
		} catch( DirectoryIteratorException exception ) {
			log.atSevere().withCause( exception ).log( "Error listing image files" );
		}
		return result;
	}

	void updateFromSettings( Settings settings ) {
		// Back layer
		configureBackPane( settings );

		// Image layer
		imagePane.setVisible( configureImagePane( settings ) );

		// Tint layer
		tintPane.setVisible( configureTintPane( settings ) );
	}

	private void configureBackPane( Settings settings ) {
		boolean backDirection = "2".equals( settings.get( "workspace-scenery-back-direction", "1" ) );
		Color backColor1 = Colors.parse( settings.get( "workspace-scenery-back-color1", "#80a0c0ff" ) );
		Color backColor2 = Colors.parse( settings.get( "workspace-scenery-back-color2", "#ffffffff" ) );
		LinearGradient backFill;
		Stop[] stops = new Stop[]{ new Stop( 0, backColor1 ), new Stop( 1, backColor2 ) };
		if( backDirection ) {
			// Horizontal
			backFill = new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops );
		} else {
			// Vertical
			backFill = new LinearGradient( 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops );
		}
		backPane.setBackground( new Background( new BackgroundFill( backFill, null, null ) ) );
	}

	private boolean configureImagePane( Settings settings ) {
		boolean imageEnabled = Boolean.parseBoolean( settings.get( "workspace-scenery-image-enabled", "false" ) );

		if( !imageEnabled ) {
			imagePane.setBackground( null );
			return false;
		}

		String imageFileString = settings.get( "workspace-scenery-image-file", "" );
		String imagePathString = settings.get( "workspace-scenery-image-path", imageFileString );
		String imageIndexString = settings.get( "workspace-scenery-image-index", "0" );
		Path imagePath = FileUtil.findValidFolder( Paths.get( imagePathString ) );

		// Determine the list of image files
		List<Path> images;
		try {
			images = listImageFiles( imagePath );
		} catch( IOException exception ) {
			images = List.of();
		}
		if( images.isEmpty() ) {
			imagePane.setBackground( null );
			return false;
		}

		// Determine the image index
		int imageIndex;
		try {
			imageIndex = Integer.parseInt( imageIndexString );
		} catch( NumberFormatException exception ) {
			imageIndex = 0;
		}
		if( imageIndex < 0 | imageIndex > images.size() - 1 ) imageIndex = 0;

		image = new Image( images.get( imageIndex ).toUri().toString() );
		style = settings.get( "workspace-scenery-image-style", "fill" );
		align = settings.get( "workspace-scenery-image-align", "center" );

		log.atFine().log( "Images: count=%s index=%s path=%s", images.size(), imageIndex, imagePath );

		BackgroundImage background = null;
		BackgroundPosition position = FxUtil.parseBackgroundPosition( align );

		switch( style ) {
			case "fill": {
				// JavaFX cover
				BackgroundSize size = new BackgroundSize( BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true );
				background = new BackgroundImage( image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, size );
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

		return imageEnabled;
	}

	private boolean configureTintPane( Settings settings ) {
		boolean tintEnabled = Boolean.parseBoolean( settings.get( "workspace-scenery-tint-enabled", "false" ) );
		int tintMode = Integer.parseInt( settings.get( "workspace-scenery-tint-mode", "0" ) );
		Color tintColor1 = Colors.parse( settings.get( "workspace-scenery-tint-color1", "#ffffff80" ) );
		Color tintColor2 = Colors.parse( settings.get( "workspace-scenery-tint-color2", "#ffffff80" ) );
		LinearGradient tintFill;
		Stop[] tintStops = new Stop[]{ new Stop( 0, tintColor1 ), new Stop( 1, tintColor2 ) };

		themeTintPane.setVisible( false );
		customTintPane.setVisible( false );
		switch( tintMode ) {
			case 0: {
				// Theme
				themeTintPane.setVisible( true );
				break;
			}
			case 1: {
				// Vertical
				tintFill = new LinearGradient( 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, tintStops );
				customTintPane.setBackground( new Background( new BackgroundFill( tintFill, null, null ) ) );
				customTintPane.setVisible( true );
				break;
			}
			case 2: {
				// Horizontal
				tintFill = new LinearGradient( 0, 0, 1, 0, true, CycleMethod.NO_CYCLE, tintStops );
				customTintPane.setBackground( new Background( new BackgroundFill( tintFill, null, null ) ) );
				customTintPane.setVisible( true );
				break;
			}
		}

		return tintEnabled;
	}

}
