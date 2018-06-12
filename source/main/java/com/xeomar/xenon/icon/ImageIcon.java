package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

import java.net.URL;

public abstract class ImageIcon extends ProgramIcon {

	private String resourceImagePath;

	public ImageIcon( String resourceImagePath ) {
		this.resourceImagePath = resourceImagePath;
	}

	@Override
	protected void render() {
		URL input = getClass().getResource( resourceImagePath );
		Image image = new Image( input.toExternalForm(), getWidth(), getHeight(), true, true );

		getGraphicsContext2D().setTransform( new Affine() );
		getGraphicsContext2D().drawImage( image, 0, 0 );
	}

}
