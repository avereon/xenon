package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public class AcornIcon extends ProgramIcon {

	private double centerLine = 16;

	private double stemRadius = 1.5;

	private double stemTop = 1;

	private double capRadius = 12;

	private double capTop = 5;

	private double capBase = 9;

	private double nutRadius = 11;

	private double nutBottom = 31;

	@Override
	protected void render() {
		drawStem();
		fill( getIconFillPaint( Color.SADDLEBROWN, GradientTone.MEDIUM ) );
		draw();

		drawNut();
		fill( getIconFillPaint( Color.SADDLEBROWN, GradientTone.LIGHT ) );
		draw();

		drawCap();
		fill( getIconFillPaint( Color.SANDYBROWN, GradientTone.MEDIUM ) );
		draw();
	}

	private void drawStem() {
		startPath();
		moveTo( g( centerLine - stemRadius ), g( capTop ) );
		lineTo( g( centerLine - stemRadius ), g( stemTop ) );
		lineTo( g( centerLine + stemRadius ), g( stemTop ) );
		lineTo( g( centerLine + stemRadius ), g( capTop ) );
		closePath();
	}

	private void drawNut() {
		double a = capBase + 21;
		double b = nutBottom - 4;
		double c = centerLine - nutRadius;
		double d = centerLine + nutRadius;

		startPath();
		moveTo( g( c ), g( capBase ) );
		curveTo( g( c ), g( a ), g( centerLine - 1 ), g( b ), g( centerLine - 1 ), g( nutBottom - 1 ) );
		addArc( g( centerLine ), g( nutBottom - 1 ), g( 1 ), g( 1 ), 180, 180 );
		curveTo( g( centerLine + 1 ), g( b ), g( d ), g( a ), g( d ), g( capBase ) );
		closePath();
	}

	private void drawCap() {
		startPath();
		moveTo( g( centerLine + capRadius - 1 ), g( capBase + 1 ) );
		addArc( g( centerLine + capRadius - 1 ), g( capBase ), g( 1 ), g( 1 ), 270, 90 );
		addArc( g( centerLine ), g( capBase ), g( capRadius ), g( capBase - capTop ), 0, 180 );
		addArc( g( centerLine - capRadius + 1 ), g( capBase ), g( 1 ), g( 1 ), 180, 90 );
		closePath();
	}

	public static void main( String[] commands ) {
		save( new AcornIcon(), "target/acorn.png" );
		proof( new AcornIcon() );
		Platform.runLater( () -> {if( Window.getWindows().size() == 0 ) Platform.exit();} );
	}

}
