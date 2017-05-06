package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.util.Colors;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.StrokeLineCap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

@Deprecated
public abstract class ProgramIcon extends Canvas {

	private static final int DEFAULT_SIZE = 256;

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

	protected enum GradientShade {
		LIGHT,
		MEDIUM,
		DARK
	}

	private static Logger log = LoggerFactory.getLogger( ProgramIcon.class );

	private static double DEFAULT_STROKE_WIDTH = 1.0 / 32.0;

	private static Color DEFAULT_STROKE_COLOR = new Color( 0.2, 0.2, 0.2, 1.0 );

	private static Color DEFAULT_FILL_COLOR = new Color( 0.8, 0.8, 0.8, 1.0 );

	private double drawWidth = DEFAULT_STROKE_WIDTH;

	private Color drawColor = DEFAULT_STROKE_COLOR;

	private Color fillColor = DEFAULT_FILL_COLOR;

	private double scale;

	public ProgramIcon() {
		this( DEFAULT_SIZE );
	}

	public ProgramIcon( double size ) {
		widthProperty().addListener( ( property, oldValue, newValue ) -> fireRender() );
		heightProperty().addListener( ( property, oldValue, newValue ) -> fireRender() );
		setSize( size );
	}

	public ProgramIcon setSize( double size ) {
		setWidth( size );
		setHeight( size );
		return this;
	}

	public Image getImage() {
		// Apparently images created from the snapshot method are not usable as
		// application icons. The following workaround creates an image that is
		// usable as an application icon. It may be more efficient to create
		// images differently if they are not needed for application icons.

		int width = (int)getWidth();
		int height = (int)getHeight();

		Pane pane = new Pane( this );
		pane.setBackground( Background.EMPTY );
		Scene scene = new Scene( pane );
		scene.setFill( Color.TRANSPARENT );

		//				// Just for research, set different color backgrounds per scale
		//				int scale = Math.min( width, height );
		//				if( scale == 16 ) scene.setFillColor( Color.PURPLE );
		//				if( scale == 24 ) scene.setFillColor( Color.BLUE );
		//				if( scale == 32 ) scene.setFillColor( Color.GREEN );
		//				if( scale == 64 ) scene.setFillColor( Color.YELLOW );
		//				if( scale == 128 ) scene.setFillColor( Color.ORANGE );
		//				if( scale == 256 ) scene.setFillColor( Color.RED );

		BufferedImage buffer = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		SwingFXUtils.fromFXImage( scene.snapshot( new WritableImage( width, height ) ), buffer );
		return SwingFXUtils.toFXImage( buffer, new WritableImage( width, height ) );
	}

	protected abstract void render();

	protected void beginPath() {
		getGraphicsContext2D().beginPath();
	}

	protected void moveTo( double x, double y ) {
		getGraphicsContext2D().moveTo( scale( x ), scale( y ) );
	}

	protected void lineTo( double x, double y ) {
		getGraphicsContext2D().lineTo( scale( x ), scale( y ) );
	}

	protected void arcTo( double x1, double y1, double x2, double y2, double radius ) {
		getGraphicsContext2D().arcTo( scale( x1 ), scale( y1 ), scale( x2 ), scale( y2 ), scale( radius ) );
	}

	protected void curveTo( double xc, double yc, double x1, double y1 ) {
		getGraphicsContext2D().quadraticCurveTo( scale( xc ), scale( yc ), scale( x1 ), scale( y1 ) );
	}

	protected void curveTo( double xc1, double yc1, double xc2, double yc2, double x1, double y1 ) {
		getGraphicsContext2D().bezierCurveTo( scale( xc1 ), scale( yc1 ), scale( xc2 ), scale( yc2 ), scale( x1 ), scale( y1 ) );
	}

	protected void closePath() {
		getGraphicsContext2D().closePath();
	}

	protected void setLineWidth( double width ) {
		getGraphicsContext2D().setLineWidth( scale( width ) );
	}

	protected void setLineCap( StrokeLineCap cap ) {
		getGraphicsContext2D().setLineCap( cap );
	}

	protected void setDrawPaint( Paint paint ) {
		getGraphicsContext2D().setStroke( paint );
	}

	protected void setFillPaint( Paint paint ) {
		getGraphicsContext2D().setFill( paint );
	}

	protected void fillAndDraw() {
		fill();
		draw();
	}

	protected void fillAndDraw( Paint fillPaint ) {
		fill( fillPaint );
		draw();
	}

	protected void fillAndDraw( Paint fillPaint, Paint drawPaint ) {
		fill( fillPaint );
		draw( drawPaint );
	}

	protected void fill() {
		getGraphicsContext2D().fill();
	}

	protected void fill( Paint paint ) {
		getGraphicsContext2D().setFill( paint );
		getGraphicsContext2D().fill();
		getGraphicsContext2D().setFill( getIconFillPaint() );
	}

	protected void draw() {
		getGraphicsContext2D().stroke();
	}

	protected void draw( Paint paint ) {
		getGraphicsContext2D().setStroke( paint );
		getGraphicsContext2D().stroke();
		getGraphicsContext2D().setStroke( getIconDrawPaint() );
	}

	protected void fillOval( double x, double y, double w, double h ) {
		getGraphicsContext2D().fillOval( scale( x ), scale( y ), scale( w ), scale( h ) );
	}

	protected void drawLine( double x1, double y1, double x2, double y2 ) {
		getGraphicsContext2D().strokeLine( scale( x1 ), scale( y1 ), scale( x2 ), scale( y2 ) );
	}

	protected void drawOval( double x, double y, double w, double h ) {
		getGraphicsContext2D().strokeOval( scale( x ), scale( y ), scale( w ), scale( h ) );
	}

	protected void clearRect( double x, double y, double w, double h ) {
		getGraphicsContext2D().clearRect( scale( x ), scale( y ), scale( w ), scale( h ) );
	}

	protected double scale( double value ) {
		return scale * value;
	}

	protected double g2( double value ) {
		return value / 2d;
	}

	protected double g4( double value ) {
		return value / 4d;
	}

	protected double g8( double value ) {
		return value / 8d;
	}

	protected double g16( double value ) {
		return value / 16d;
	}

	protected double g32( double value ) {
		return value / 32d;
	}

	protected double getIconDrawWidth() {
		return drawWidth;
	}

	protected Color getIconDrawPaint() {
		return drawColor;
	}

	protected Color getIconFillPaint() {
		return fillColor;
	}

	protected Paint getIconFillPaint( GradientShade shade ) {
		double a = 1;
		double b = 0;

		switch( shade ) {
			case LIGHT: {
				a = 0.8;
				b = 0.5;
				break;
			}
			case MEDIUM: {
				a = 0.65;
				b = 0.35;
				break;
			}
			case DARK: {
				a = 0.5;
				b = 0.2;
				break;
			}
		}

		Color colorA = Colors.getShade( getIconFillPaint(), a );
		Color colorB = Colors.getShade( getIconFillPaint(), b );

		return getGradientPaint( colorA, colorB );
	}

	private Paint getGradientPaint( Color a, Color b ) {
		return new LinearGradient( 0, 0, scale( 1 ), scale( 1 ), false, CycleMethod.NO_CYCLE, new Stop( 0, a ), new Stop( 1, b ) );
	}

	private void fireRender() {
		scale = Math.min( getWidth(), getHeight() );

		// Set the defaults
		setLineCap( StrokeLineCap.ROUND );
		setLineWidth( getIconDrawWidth() );
		setDrawPaint( getIconDrawPaint() );
		setFillPaint( getIconFillPaint( GradientShade.LIGHT ) );

		// Start rendering by clearing the icon area
		clearRect( 0, 0, 1, 1 );

		//		// Just for research, set different color backgrounds per scale
		//		double scale = Math.min( getWidth(), getHeight() );
		//		if( scale == 16 ) protected void setFillColor( Color.PURPLE );
		//		if( scale == 24 ) protected void setFillColor( Color.BLUE );
		//		if( scale == 32 ) protected void setFillColor( Color.GREEN );
		//		if( scale == 64 ) protected void setFillColor( Color.YELLOW );
		//		if( scale == 128 ) protected void setFillColor( Color.ORANGE );
		//		if( scale == 256 ) protected void setFillColor( Color.RED );
		//		protected void fillRect( 0, 0, getWidth(), getHeight() );

		render();
	}

}
