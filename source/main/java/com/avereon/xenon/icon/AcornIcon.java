package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;
import javafx.application.Platform;
import javafx.stage.Window;

public class AcornIcon extends ProgramIcon {

	@Override
	protected void render() {
		drawStem();
		fillAndDraw( GradientTone.DARK );

		drawNut();
		fillAndDraw( GradientTone.MEDIUM );

		drawCap();
		fillAndDraw( GradientTone.LIGHT );
	}

	private void drawStem() {
		startPath();
		moveTo( g( 15 ), g( 7 ) );
		lineTo( g( 15 ), g( 3 ) );
		lineTo( g( 17 ), g( 3 ) );
		lineTo( g( 17 ), g( 7 ) );
		closePath();
	}

	private void drawNut() {
		double a = 30;
		double b = 25;
		double c = 7;
		double d = 25;

		startPath();
		moveTo( g( c ), g( 11 ) );
		curveTo( g( c ), g( a ), g( 15 ), g( b ), g( 15 ), g( 28 ) );
		addArc( g( 16 ), g( 28 ), g( 1 ), g( 1 ), 180, 180 );
		curveTo( g( 17 ), g( b ), g( d ), g( a ), g( d ), g( 11 ) );
		closePath();
	}

	private void drawCap() {
		startPath();
		moveTo( g( 25 ), g( 12 ) );
		addArc( g( 25 ), g( 11 ), g( 1 ), g( 1 ), 270, 90 );
		addArc( g( 16 ), g( 11 ), g( 10 ), g( 4 ), 0, 180 );
		addArc( g( 7 ), g( 11 ), g( 1 ), g( 1 ), 180, 90 );
		closePath();
	}

	public static void main( String[] commands ) {
		save( new AcornIcon(), "target/acorn.png" );
		proof( new AcornIcon() );
		Platform.runLater( () -> {if( Window.getWindows().size() == 0 ) Platform.exit();} );
	}

}
