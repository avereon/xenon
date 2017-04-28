package com.parallelsymmetry.essence;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

public abstract class IconRenderer extends Canvas {

	private static final int DEFAULT_SIZE = 24;

	private static final SnapshotParameters snapshotParameters;

	static {
		snapshotParameters = new SnapshotParameters();
		snapshotParameters.setFill( Color.TRANSPARENT );
	}

	public IconRenderer() {
		this( DEFAULT_SIZE );
	}

	public IconRenderer( double size ) {
		widthProperty().addListener( ( property, oldValue, newValue ) -> render() );
		heightProperty().addListener( ( property, oldValue, newValue ) -> render() );
		setSize( size );
	}

	public void setSize( double size ) {
		setWidth( size );
		setHeight( size );
	}

	protected abstract void render( GraphicsContext gfx );

	protected int scale( double value ) {
		return (int)(Math.min( getWidth(), getHeight() ) * value);
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

	static Image getImage( IconRenderer renderer, int size ) {
		renderer.setSize( size );

		// WORKAROUND
		// FIXME This workaround does work. It's not very pretty. Can it be cleaned up?
		Pane pane = new Pane(renderer);
		pane.setBackground( Background.EMPTY );
		Scene scene = new Scene( pane );
		scene.setFill( Color.TRANSPARENT );

		BufferedImage buffer = new BufferedImage( size, size, BufferedImage.TYPE_INT_ARGB );
		SwingFXUtils.fromFXImage( scene.snapshot( new WritableImage( size, size ) ), buffer );
		return SwingFXUtils.toFXImage( buffer, new WritableImage( size, size ) );

		//return renderer.snapshot( snapshotParameters, null );
	}

	private void render() {
		GraphicsContext gfx = getGraphicsContext2D();
		gfx.clearRect( 0, 0, getWidth(), getHeight() );
		render( gfx );
	}

}
