package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.IconRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class BrokenIcon extends IconRenderer {

	public BrokenIcon() {
		super();
	}

	public BrokenIcon( double size ) {
		super( size );
	}

	protected void render( GraphicsContext gfx) {
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
