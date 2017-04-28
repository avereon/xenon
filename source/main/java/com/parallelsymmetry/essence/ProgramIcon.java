package com.parallelsymmetry.essence;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public abstract class ProgramIcon {

	private static final int DEFAULT_SIZE = 256;

	private int size;

	public ProgramIcon() {
		this( DEFAULT_SIZE );
	}

	public ProgramIcon( int size ) {
		this.size = size;
	}

	public abstract void paint( GraphicsContext gfx );

	public Image getImage() {
		Canvas canvas = new Canvas( size, size );
		paint( canvas.getGraphicsContext2D() );

		WritableImage image = new WritableImage( size, size );
		canvas.snapshot( new SnapshotParameters(), image );

		return image;
	}

	protected int scale( double value ) {
		return (int)(size * value);
	}

}
