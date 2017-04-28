package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class BrokenIcon extends ProgramIcon {

	public BrokenIcon( int size ) {
		super( size );
	}

	public void paint( GraphicsContext gfx ) {
		gfx.setFill( Color.RED );
		gfx.fillRect( 0, 0, scale( 1 ), scale( 1 ) );

		gfx.setStroke( Color.RED.darker() );
		gfx.setLineCap( StrokeLineCap.ROUND );
		gfx.setLineWidth( scale( 1d / 16d ) );

		gfx.strokeLine( scale( 1d / 8d ), scale( 1d / 8d ), scale( 7d / 8d ), scale( 7d / 8d ) );
		gfx.strokeLine( scale( 7d / 8d ), scale( 1d / 8d ), scale( 1d / 8d ), scale( 7d / 8d ) );
	}

}
