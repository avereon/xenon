package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;

import java.net.URL;

public class OsLinuxIcon extends ProgramIcon {

	@Override
	protected void render() {
		URL input = getClass().getResource( "/icons/linux.png" );
		Image image = new Image( input.toExternalForm(), getWidth(), getHeight(), true, true );

		getGraphicsContext2D().setTransform( new Affine() );
		getGraphicsContext2D().drawImage( image, 0, 0 );
	}

	public static void main( String[] commands ) {
		proof( new OsLinuxIcon() );
	}

}
