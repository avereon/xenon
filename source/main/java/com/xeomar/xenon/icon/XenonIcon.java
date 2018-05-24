package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class XenonIcon extends XLargeIcon {

	@Override
	protected void render() {
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
		jetPaintStops.add( new Stop( 9.0, jetTip ) );
		Paint jetPaint = radialPaint( g( 16 ), g( 16 ), jetRadius, jetPaintStops );

		// Ring
		double outer = 15;
		double inner = outer - 8;
		double shrink = 3;

		beginPath();
		addArc( g( 16 ), g( 16 ), g( outer ), g( outer - shrink ), 0, 360 );
		moveTo( g( 16 + inner ), g( 16 ) );
		addArc( g( 16 ), g( 16 ), g( inner ), g( inner - shrink ), 0, 360 );
		closePath();
		fillAndDraw( ringBase );

		xPath();
		fillAndDraw( jetPaint );
	}

	public static void main( String[] commands ) {
		proof( new XenonIcon() );
		//		save( new XenonIcon(), "../../software/xenon/source/main/resources/xenon.png");
	}

}
