package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import javafx.scene.image.Image;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

public class OsLinuxIcon extends ProgramIcon {

	@Override
	protected void render() {
//		URL input = getClass().getResource( "/icons/linux.png" );
//		Image image = new Image( input.toExternalForm(), getWidth(), getHeight(), true, true );
//
//		getGraphicsContext2D().setTransform( new Affine() );
//		getGraphicsContext2D().drawImage( image, 0, 0 );

//		try {
//			String content = IOUtils.toString( getClass().getResource( "/icons/linux.svg" ) );
//			SVGPath path = new SVGPath();
//			path.setContent( content );
//		} catch( IOException e ) {
//			e.printStackTrace();
//		}
	}

	public static void main( String[] commands ) {
		proof( new OsLinuxIcon() );
	}

}
