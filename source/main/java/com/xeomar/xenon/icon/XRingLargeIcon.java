package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * Use <a href="http://www.pic2icon.com/">Pic2Icon</a> to convert to Windows icon.
 */
public class XRingLargeIcon extends XLargeIcon {

	protected void render() {
		double outerRingMax = 15;
		double outerRingMin = 11;
		double innerRingMax = 7;
		double innerRingMin = 5;

		// Ring colors
		Color ringBase = Color.web( "#4FC3F7" );
		Color ringHighlight = Color.web( "#81D4FA" );

		// Jet colors
		Color jetCenter = Color.web( "#FFF59D" );
		Color jetTip = Color.web( "#FFA726" );

		// Jet paint
		double jetRadius = Math.sqrt( 2 * (g( 13 ) * g( 13 )) );
		List<Stop> jetPaintStops = new ArrayList<>();
		jetPaintStops.add( new Stop( 0.1, jetCenter ) );
		jetPaintStops.add( new Stop( 1.0, jetTip ) );
		Paint jetPaint = radialPaint( g( 16 ), g( 16 ), jetRadius, jetPaintStops );

		// Ring paint
		List<Stop> ringPaintStops = new ArrayList<>();
		ringPaintStops.add( new Stop( 0.6, ringHighlight ) );
		ringPaintStops.add( new Stop( 0.9, ringBase ) );
		Paint ringPaint = radialPaint( g( 16 ), g( 16 ) * outerRingMax / outerRingMin, g( outerRingMax ), ringPaintStops );

		// Bottom of jet
		getGraphicsContext2D().save();
		clip( g( 0 ), g( 16 ), g( 32 ), g( 16 ) );
		xPath();
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

		// Top of jet
		getGraphicsContext2D().save();
		clip( g( 0 ), g( 0 ), g( 32 ), g( 16 ) );
		xPath();
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
