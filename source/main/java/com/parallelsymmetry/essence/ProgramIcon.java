package com.parallelsymmetry.essence;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public abstract class ProgramIcon {

	private static final int DEFAULT_SIZE = 256;

	private int size;

	public ProgramIcon() {
		this.size = DEFAULT_SIZE;
	}

	public abstract void paint( GraphicsContext gfx );

	public Image getImage() {
		return getImage( this.size );
	}

	public Image getImage( int size ) {
		// Create and paint the canvas
		Canvas canvas = new Canvas( size, size );
		paint( canvas.getGraphicsContext2D() );

		// Create the snapshot parameters
		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setFill( Color.TRANSPARENT );

		// Create and render the image
		WritableImage image = new WritableImage( size, size );
		canvas.snapshot( snapshotParameters, image );

		return image;
	}

	protected int scale( double value ) {
		return (int)(size * value);
	}

	protected int scale8( double value ) {
		return scale( value / 8d );
	}

	protected int scale16( double value ) {
		return scale( value / 16d );
	}

	protected int scale32( double value ) {
		return scale( value / 32d );
	}

}
