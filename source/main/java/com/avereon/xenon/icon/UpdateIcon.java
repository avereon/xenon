package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;
import com.avereon.venza.javafx.JavaFxStarter;

public class UpdateIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Tray
		startPath();
		moveTo( g( 3 ), g( 21 ) );
		lineTo( g( 7 ), g( 21 ) );
		lineTo( g( 7 ), g( 23 ) );
		addArc( g( 9 ), g( 23 ), g( 2 ), g( 2 ), 180, 90 );
		lineTo( g( 23 ), g( 25 ) );
		addArc( g( 23 ), g( 23 ), g( 2 ), g( 2 ), 270, 90 );
		lineTo( g( 25 ), g( 21 ) );
		lineTo( g( 29 ), g( 21 ) );
		lineTo( g( 29 ), g( 23 ) );
		addArc( g( 23 ), g( 23 ), g( 6 ), g( 6 ), 0, -90 );
		lineTo( g( 9 ), g( 29 ) );
		addArc( g( 9 ), g( 23 ), g( 6 ), g( 6 ), 270, -90 );
		closePath();
		fillAndDraw();

		// Arrow
		startPath();
		moveTo( g( 13 ), g( 11 ) );
		lineTo( g( 19 ), g( 11 ) );
		lineTo( g( 19 ), g( 15 ) );
		lineTo( g( 23 ), g( 15 ) );
		lineTo( g( 16 ), g( 22 ) );
		lineTo( g( 9 ), g( 15 ) );
		lineTo( g( 13 ), g( 15 ) );
		closePath();
		fillAndDraw();

		// Package
		fillOval( g( 13 ), g( 1 ), g( 6 ), g( 6 ) );
		drawOval( g( 13 ), g( 1 ), g( 6 ), g( 6 ) );
		//		beginPath();
		//		moveTo( g( 13 ), g( 3 ) );
		//		lineTo( g( 19 ), g( 3 ) );
		//		lineTo( g( 19 ), g( 9 ) );
		//		lineTo( g( 13 ), g( 9 ) );
		//		closePath();
		//		fillAndDraw();
	}

	public static void main( String[] commands ) {
		JavaFxStarter.startAndWait( 1000 );
		proof( new UpdateIcon() );
	}

}
