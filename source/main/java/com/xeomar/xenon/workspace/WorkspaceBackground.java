package com.xeomar.xenon.workspace;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.util.Colors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkspaceBackground extends Pane {

	private Pane backPane;

	private ImageView imagePane;

	private Pane tintPane;

	private String style = "fill";

	private String align = "center";

	public WorkspaceBackground() {
		backPane = new Pane();
		imagePane = new ImageView();
		tintPane = new Pane();

		imagePane.setVisible( false );
		tintPane.setVisible( false );

		backPane.prefWidthProperty().bind( this.widthProperty() );
		backPane.prefHeightProperty().bind( this.heightProperty() );
		tintPane.prefWidthProperty().bind( this.widthProperty() );
		tintPane.prefHeightProperty().bind( this.heightProperty() );

		getChildren().addAll( backPane, imagePane, tintPane );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		Image image = imagePane.getImage();
		if( image == null ) return;

		boolean tallSpace = getHeight() > getWidth();

		boolean tall = image.getHeight() > image.getWidth();
		switch( style ) {
			case "fill": {
				if( tallSpace ) {
					imagePane.setFitWidth( getWidth() );
				} else {
					imagePane.setFitHeight( getHeight() );
				}
				break;
			}
			case "fit": {
				//				imagePane.fitHeightProperty().bind( this.heightProperty() );
				//				imagePane.fitWidthProperty().bind( this.widthProperty() );
				//				imagePane.setPreserveRatio( true );
				break;
			}
			case "stretch": {
				//				imagePane.fitWidthProperty().bind( this.widthProperty() );
				//				imagePane.fitHeightProperty().bind( this.heightProperty() );
				//				imagePane.setPreserveRatio( false );
				break;
			}
			case "tile": {
				// Not implemented
				break;
			}
			case "anchor": {
				// No scaling should be applied, just anchor the image according to the align parameter\
				//				imagePane.fitWidthProperty().unbind();
				//				imagePane.fitHeightProperty().unbind();
				//				imagePane.setFitWidth( image.getWidth() );
				//				imagePane.setFitHeight( image.getHeight() );
				//				imagePane.setPreserveRatio( true );
				break;
			}
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

		String style = settings.get( "workspace-scenery-image-style", "fill" );
		String align = settings.get( "workspace-scenery-image-align", "center" );

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

		imagePane.setImage( image );
		imagePane.setVisible( imageEnabled );
		setImageConstraints( image, style, align );

		tintPane.setBackground( new Background( new BackgroundFill( tintFill, null, null ) ) );
		tintPane.setVisible( tintEnabled );
	}

	private void setImageConstraints( Image image, String style, String align ) {
		imagePane.setPreserveRatio( !"stretch".equals( style ) );
	}

}
