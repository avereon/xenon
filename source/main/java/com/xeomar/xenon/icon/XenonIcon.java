package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class XenonIcon extends XIcon {

	@Override
	protected void render() {
		// Jet paint
		double jetArm = 10;
		double jetRadius = Math.sqrt( 2 * (jetArm * jetArm) );
		List<Stop> jetPaintStops = new ArrayList<>();
//		jetPaintStops.add( new Stop( 0.1, Color.web( "#eeeeee" ) ) );
//		jetPaintStops.add( new Stop( 0.4, Color.web( "#709acc" ) ) );
//		jetPaintStops.add( new Stop( 0.8, Color.web( "#cca0ff" ) ) );
		jetPaintStops.add( new Stop( 0.1, Color.web( "#eeeeee" ) ) );
		jetPaintStops.add( new Stop( 0.4, Color.web( "#eeee00" ) ) );
		jetPaintStops.add( new Stop( 0.8, Color.web( "#ffaa00" ) ) );
		Paint jetPaint = radialPaint( g( 16 ), g( 16 ), g(jetRadius), jetPaintStops );

		double r = 11;

		beginPath();
		addArc( g( 16 ), g( 16 ), g( r ), g( r-3 ), 0, 360 );
		moveTo( g( 16 + r - 4 ), g( 16 ) );
		addArc( g( 16 ), g( 16 ), g( r - 4 ), g( r - 7 ), 0, 360 );
		closePath();
		fillAndDraw( Color.web( "#cca0ff" ) );

		xPath();
		fillAndDraw( jetPaint );
	}

	public static void main( String[] commands ) {
		proof( new XenonIcon() );
//		save( new XenonIcon(), "../../software/xenon/source/main/resources/program.png");
	}

}
