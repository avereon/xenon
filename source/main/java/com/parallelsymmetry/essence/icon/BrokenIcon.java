package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class BrokenIcon extends ProgramIcon {

	protected void render() {
		double min = g8( 2 );
		double max = g8( 6 );

		setStroke( Color.MAGENTA );
		setLineCap( StrokeLineCap.ROUND );
		setLineWidth( g16( 3d ) );

		drawLine( min, min, max, max );
		drawLine( max, min, min, max );
	}

}
