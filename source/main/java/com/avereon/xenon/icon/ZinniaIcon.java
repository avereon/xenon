package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ZinniaIcon extends ProgramIcon {

	private double centerRadius = 3;

	private int likeness = 5;

	private int layers= 2;

	@Override
	protected void render() {
		double spacing = 360.0 / likeness;
		double offset = spacing/layers;

		for( int layer = 0; layer < layers; layer++ ) {
			// Lower layer
			for( int index = 0; index < likeness; index++ ) {
				drawPetal();
				spin( g( 16 ), g( 16 ), spacing );
			}
			// Upper layer
			spin( g( 16 ), g( 16 ), offset );
		}
		reset();

		drawCenter();
	}

	private void drawPetal() {
		double n = 5;

		startPath();
		moveTo( g( 15 ), g( 15 ) );
		curveTo( g( 14 ), g( 8 ), g( 16 - n ), g( 1 ), g( 16 ), g( 1 ) );
		curveTo( g( 16 + n ), g( 1 ), g( 18 ), g( 8 ), g( 17 ), g( 15 ) );
		closePath();

		fillAndDraw( Color.HOTPINK, Color.HOTPINK.darker() );
	}

	private void drawCenter() {
		Paint centerPaint = Color.SADDLEBROWN.darker();
		setDrawPaint( centerPaint );
		setFillPaint( centerPaint );
		fillCenteredOval( g( 16 ), g( 16 ), g( centerRadius ), g( centerRadius ) );
		drawCenteredOval( g( 16 ), g( 16 ), g( centerRadius ), g( centerRadius ) );

		drawStamen( 10 );
		drawStamen( 130 );
		drawStamen( 250 );
	}

	private void drawStamen( double angleInDegrees ) {
		double radius = 1.8;

		double x = radius * Math.cos( angleInDegrees * RADIANS_PER_DEGREE );
		double y = radius * Math.sin( angleInDegrees * RADIANS_PER_DEGREE );

		setDrawPaint( Color.YELLOW );
		drawDot( g( 16 + x ), g( 16 + y ) );
	}

	public static void main( String[] commands ) {
		save( new ZinniaIcon(), "target/icons/zinnia.png" );
		proof( new ZinniaIcon() );
		wrapup();
	}

}
