package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class AboutProgramIcon extends ProgramIcon {

	protected void render( GraphicsContext gfx ) {
		double stroke = scale16( 3d );
		double min = scale8( 2d );
		double max = scale8( 6d );

		gfx.setStroke( Color.CORNFLOWERBLUE );
		gfx.setLineCap( StrokeLineCap.ROUND );
		gfx.setLineWidth( stroke );

		gfx.strokeLine( min, min, max, max );
		gfx.strokeLine( max, min, min, max );
	}

}
