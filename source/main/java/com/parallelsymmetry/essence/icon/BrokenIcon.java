package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class BrokenIcon extends ProgramIcon {

	public void paint( GraphicsContext gfx ) {
		gfx.setFill( Color.TRANSPARENT );
		gfx.fillRect( 0, 0, scale( 1 ), scale( 1 ) );

		double stroke = scale( 3d / 16d );
		double min = scale( 2d / 8d );
		double max = scale( 6d / 8d );

		gfx.setStroke( Color.RED.darker() );
		gfx.setLineCap( StrokeLineCap.ROUND );
		gfx.setLineWidth( stroke );

		gfx.strokeLine( min, min, max, max );
		gfx.strokeLine( max, min, min, max );
	}

}
