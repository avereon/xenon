package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * Use <a href="http://www.pic2icon.com/windows7_vista_icon_generator.php">Pic2Icon</a> to convert to Windows icon.
 */
public class XRingLargeIcon extends XLargeIcon {

	protected void render() {
		double outerRingMax = 15;
		double outerRingMin = 11;
		double innerRingMax = 7;
		double innerRingMin = 5;

		// Xenon hue: 263
		//Color dkXenon = Color.web( "#9965e6" );
		//Color mdXenon = Color.web( "#b691ed" );
		//Color ltXenon = Color.web( "#d3bdf4" );

		Color mdRing = Color.web( "#80a0c0");
		Color ltRing = Color.web( "#84d1f9");

		Color jetCenter = Color.web( "#ffffcc");
		Color jetMiddle = Color.web( "#ffff80");
		Color jetTip = Color.web( "#ff8040");

		// Jet paint
		double jetRadius = Math.sqrt( 2 * (g( 13 ) * g( 13 )) );
		List<Stop> jetPaintStops = new ArrayList<>();
		//		jetPaintStops.add( new Stop( 0.1, Color.web( "#eeeeee" ) ) );
		//		jetPaintStops.add( new Stop( 0.4, Color.web( "#709acc" ) ) );
		//		jetPaintStops.add( new Stop( 0.8, Color.web( "#aa80ff" ) ) );
		jetPaintStops.add( new Stop( 0.1, jetCenter ) );
		//jetPaintStops.add( new Stop( 0.5, jetMiddle ) );
		jetPaintStops.add( new Stop( 1.0, jetTip ) );
		Paint jetPaint = radialPaint( g( 16 ), g( 16 ), jetRadius, jetPaintStops );

		// Ring paint
		List<Stop> ringPaintStops = new ArrayList<>();
		ringPaintStops.add( new Stop( 0.6, ltRing ) );
		ringPaintStops.add( new Stop( 0.9, mdRing ) );
		Paint ringPaint = radialPaint( g( 16 ), g( 16 ) * outerRingMax / outerRingMin, g( outerRingMax ), ringPaintStops );

		// Bottom of jet
		getGraphicsContext2D().save();
		clip( g( 0 ), g( 16 ), g( 32 ), g( 16 ) );
		xPath();
		//fillAndDraw( getIconFillPaint( GradientShade.LIGHT ) );
		fillAndDraw( jetPaint );
		getGraphicsContext2D().restore();

		// Ring
		beginPath();
		addArc( g( 16 ), g( 16 ), g( outerRingMax ), g( outerRingMin ), 0, 360 );
		moveTo( g( 16 + innerRingMax ), g( 16 ) );
		addArc( g( 16 ), g( 16 ), g( innerRingMax ), g( innerRingMin ), 0, 360 );
		closePath();
		getGraphicsContext2D().save();
		getGraphicsContext2D().scale( 1, outerRingMin / outerRingMax );
		fillAndDraw( ringPaint );
		getGraphicsContext2D().restore();
		//draw();

		// Top of jet
		getGraphicsContext2D().save();
		clip( g( 0 ), g( 0 ), g( 32 ), g( 16 ) );
		xPath();
		//fillAndDraw( getIconFillPaint( GradientShade.LIGHT ) );
		fillAndDraw( jetPaint );
		getGraphicsContext2D().restore();
	}

	private void clip( double x, double y, double w, double h ) {
		beginPath();
		addRect( x, y, w, h );
		closePath();
		clip();
	}

	public static void main( String[] commands ) {
		proof( new XRingLargeIcon() );
		//save( new XRingIcon(), "Downloads/program.png" );
		//save( new XRingIcon(), "../../software/xenon/source/main/resources/program.png");
		//save( new XRingIcon(), "../../software/xenon/source/main/assembly/program.png");
	}

}
