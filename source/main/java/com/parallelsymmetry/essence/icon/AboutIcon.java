package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class AboutIcon extends ProgramIcon {

	public void paint( GraphicsContext gfx ) {
		gfx.setStroke( Color.RED.darker() );
		gfx.setLineCap( StrokeLineCap.ROUND );
		gfx.setLineWidth( 8 );

		gfx.strokeLine( 8, 8, 248, 248 );
		gfx.strokeLine( 248, 8, 8, 248 );
	}

}
