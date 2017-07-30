package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * Use <a href="http://www.pic2icon.com/windows7_vista_icon_generator.php">Pic2Icon</a> to convert to Windows icon.
 */
public class XRingIcon extends XIcon {

	protected void render() {
		double ringScale = 7.0 / 11.0;

		// Xenon hue: 263
		Color dkXenon = Color.web( "#7836e5" );
		Color mdXenon = Color.web( "#a77cf6" );
		Color ltXenon = Color.web( "#b874e6" );

		// Jet paint
		double jetRadius = Math.sqrt( 2 * (g( 10 ) * g( 10 )) );
		List<Stop> jetPaintStops = new ArrayList<>();
		jetPaintStops.add( new Stop( 0.1, Color.web( "#eeeeee" ) ) );
		jetPaintStops.add( new Stop( 0.4, Color.web( "#709acc" ) ) );
		jetPaintStops.add( new Stop( 0.8, Color.web( "#aa80ff" ) ) );
		Paint jetPaint = radialPaint( g( 16 ), g( 16 ), jetRadius, jetPaintStops );

		// Ring paint
		List<Stop> ringPaintStops = new ArrayList<>();
		ringPaintStops.add( new Stop( 0.6, Color.web( "#ffee80" ) ) );
		ringPaintStops.add( new Stop( 0.9, Color.web( "#ff6000" ) ) );
		Paint ringPaint = radialPaint( g( 16 ), (1 / ringScale) * g( 16 ), g( 11 ), ringPaintStops );

		// Bottom of jet
		getGraphicsContext2D().save();
		clip( g( 0 ), g( 16 ), g( 32 ), g( 16 ) );
		xPath();
		fillAndDraw( getIconFillPaint( GradientShade.LIGHT ) );
		getGraphicsContext2D().restore();

		// Ring
		beginPath();
		addArc( g( 16 ), g( 16 ), g( 11 ), g( ringScale * 11 ), 0, 360 );
		moveTo( g( 21 ), g( 16 ) );
		addArc( g( 16 ), g( 16 ), g( 5 ), g( 2.5 ), 0, 360 );
		closePath();
		getGraphicsContext2D().save();
		getGraphicsContext2D().scale( 1, ringScale );
		fill();
		getGraphicsContext2D().restore();
		draw();

		// Top of jet
		getGraphicsContext2D().save();
		clip( g( 0 ), g( 0 ), g( 32 ), g( 16 ) );
		xPath();
		fillAndDraw( getIconFillPaint( GradientShade.LIGHT ) );
		getGraphicsContext2D().restore();
	}

	private void clip( double x, double y, double w, double h ) {
		beginPath();
		addRect( x, y, w, h );
		closePath();
		clip();
	}

	public static void main( String[] commands ) {
		//proof( new XRingIcon() );
		new XRingIcon().save( "Downloads/program.png");
	}

}
