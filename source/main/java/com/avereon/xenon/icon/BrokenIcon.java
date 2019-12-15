package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class BrokenIcon extends ProgramIcon {

	protected void render() {
		double min = g( 8 );
		double max = g( 24 );

		setDrawPaint( Color.MAGENTA );
		setLineCap( StrokeLineCap.ROUND );
		setDrawWidth( g( 6 ) );

		drawLine( min, min, max, max );
		drawLine( max, min, min, max );
	}

	public static void main( String[] commands ) {
		proof( new BrokenIcon() );
	}

}
