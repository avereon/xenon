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
import javafx.scene.shape.StrokeLineCap;

import java.awt.image.BufferedImage;

@Deprecated
public abstract class OldProgramIcon extends Canvas {

	public static final double A = 0;

	public static final double B = 1;

	public static final double C = 0.5;

	public static final double D = 0.25;

	public static final double E = 0.75;

	public static final double F = 0.125;

	public static final double G = 0.375;

	public static final double H = 0.625;

	public static final double I = 0.875;

	public static final double J = 0.0625;

	public static final double K = 0.1875;

	public static final double L = 0.3125;

	public static final double M = 0.4375;

	public static final double N = 0.5625;

	public static final double O = 0.6875;

	public static final double P = 0.8125;

	public static final double Q = 0.9375;

	public static final double ZA = 0.03125;

	public static final double ZB = 0.09375;

	public static final double ZC = 0.15625;

	public static final double ZD = 0.21875;

	public static final double ZE = 0.28125;

	public static final double ZF = 0.34375;

	public static final double ZG = 0.40625;

	public static final double ZH = 0.46875;

	public static final double ZI = 0.53125;

	public static final double ZJ = 0.59375;

	public static final double ZK = 0.65625;

	public static final double ZL = 0.71875;

	public static final double ZM = 0.78125;

	public static final double ZN = 0.84375;

	public static final double ZO = 0.90625;

	public static final double ZP = 0.96875;

	private static final int DEFAULT_SIZE = 256;

	private static final SnapshotParameters snapshotParameters;

	static {
		snapshotParameters = new SnapshotParameters();
		snapshotParameters.setFill( Color.TRANSPARENT );
	}

	public OldProgramIcon() {
		this( DEFAULT_SIZE );
	}

	public OldProgramIcon( double size ) {
		widthProperty().addListener( ( property, oldValue, newValue ) -> render() );
		heightProperty().addListener( ( property, oldValue, newValue ) -> render() );
		setSize( size );
	}

	public OldProgramIcon setSize( double size ) {
		setWidth( size );
		setHeight( size );
		return this;
	}

	public static Image getImage( OldProgramIcon renderer ) {
		// Apparently images created from the snapshot method are not usable as
		// application icons. The following workaround creates an image that is
		// usable as an application icon. It may be more efficient to create
		// images differently if they are not needed for application icons.

		int width = (int)renderer.getWidth();
		int height = (int)renderer.getHeight();

		Pane pane = new Pane( renderer );
		pane.setBackground( Background.EMPTY );
		Scene scene = new Scene( pane );
		scene.setFill( Color.TRANSPARENT );

		//		// Just for research, set different color backgrounds per size
		//		int size = Math.min( width, height );
		//		if( size == 16 ) scene.setFill( Color.PURPLE );
		//		if( size == 24 ) scene.setFill( Color.BLUE );
		//		if( size == 32 ) scene.setFill( Color.GREEN );
		//		if( size == 64 ) scene.setFill( Color.YELLOW );
		//		if( size == 128 ) scene.setFill( Color.ORANGE );
		//		if( size == 256 ) scene.setFill( Color.RED );

		BufferedImage buffer = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		SwingFXUtils.fromFXImage( scene.snapshot( new WritableImage( width, height ) ), buffer );
		return SwingFXUtils.toFXImage( buffer, new WritableImage( width, height ) );
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

	private void render() {
		GraphicsContext gfx = getGraphicsContext2D();
		gfx.clearRect( 0, 0, getWidth(), getHeight() );
		gfx.setLineCap( StrokeLineCap.ROUND );
		gfx.setLineWidth( scale16( 1 ) );

		//		// Just for research, set different color backgrounds per size
		//		double size = Math.min( getWidth(), getHeight() );
		//		if( size == 16 ) gfx.setFill( Color.PURPLE );
		//		if( size == 24 ) gfx.setFill( Color.BLUE );
		//		if( size == 32 ) gfx.setFill( Color.GREEN );
		//		if( size == 64 ) gfx.setFill( Color.YELLOW );
		//		if( size == 128 ) gfx.setFill( Color.ORANGE );
		//		if( size == 256 ) gfx.setFill( Color.RED );
		//		gfx.fillRect( 0, 0, getWidth(), getHeight() );

		render( gfx );
	}

}
