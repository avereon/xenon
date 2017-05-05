package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class BrokenIcon extends ProgramIcon {

	protected void configure( Group group ) {
		double stroke = g16( 3d );
		double min = g8( 2d );
		double max = g8( 6d );

		Line a = new Line( min, min, max, max );
		Line b = new Line( min, max, max, min );

		a.setStrokeLineCap( StrokeLineCap.ROUND );
		a.setStrokeWidth( stroke );
		a.setStroke( Color.MAGENTA );

		b.setStrokeLineCap( StrokeLineCap.ROUND );
		b.setStrokeWidth( stroke );
		b.setStroke( Color.MAGENTA );

		add( a, b );
	}

}
