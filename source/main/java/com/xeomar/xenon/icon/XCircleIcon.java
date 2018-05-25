package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class XCircleIcon extends XLargeIcon {

	@Override
	protected void render() {
		// Ring colors
		// Blue 200
		Color ringHighlight = Color.web( "#90CAF9" );
		// Blue 400
		Color ringBase = Color.web( "#42A5F5" );

		// Jet colors
		// Yellow A200
		Color jetCenter = Color.web( "#FFF59D" );
		// Orange 500
		Color jetTip = Color.web( "#FF9800" );

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
		proof( new XCircleIcon() );
		//save( new XCircleIcon(), "../../software/xenon/source/main/resources/xenon.png");
	}

}
