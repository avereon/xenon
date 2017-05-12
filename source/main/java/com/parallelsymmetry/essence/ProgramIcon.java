package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.util.Colors;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
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

	private double size;

	private double xOffset;

	private double yOffset;

	public ProgramIcon() {
		this( DEFAULT_SIZE );
	}

	public ProgramIcon( double size ) {
		widthProperty().addListener( ( property, oldValue, newValue ) -> fireRender() );
		heightProperty().addListener( ( property, oldValue, newValue ) -> fireRender() );
		setSize( size );
	}

	@Override
	public ProgramIcon clone() {
		ProgramIcon clone = null;

		try {
			clone = getClass().getDeclaredConstructor().newInstance();
			clone.setWidth( getWidth() );
			clone.setHeight( getHeight() );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return clone;
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

		//				// Just for research, set different color backgrounds per xformX
		//				int xformX = Math.min( width, height );
		//				if( xformX == 16 ) scene.setFillColor( Color.PURPLE );
		//				if( xformX == 24 ) scene.setFillColor( Color.BLUE );
		//				if( xformX == 32 ) scene.setFillColor( Color.GREEN );
		//				if( xformX == 64 ) scene.setFillColor( Color.YELLOW );
		//				if( xformX == 128 ) scene.setFillColor( Color.ORANGE );
		//				if( xformX == 256 ) scene.setFillColor( Color.RED );

		BufferedImage buffer = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		SwingFXUtils.fromFXImage( scene.snapshot( new WritableImage( width, height ) ), buffer );
		return SwingFXUtils.toFXImage( buffer, new WritableImage( width, height ) );
	}

	protected abstract void render();

	protected void reset() {
		xOffset = 0;
		yOffset = 0;
	}

	protected void move( double x, double y ) {
		xOffset = x;
		yOffset = y;
	}

	protected void beginPath() {
		getGraphicsContext2D().beginPath();
	}

	protected void moveTo( double x, double y ) {
		getGraphicsContext2D().moveTo( xformX( x ), xformY( y ) );
	}

	protected void lineTo( double x, double y ) {
		getGraphicsContext2D().lineTo( xformX( x ), xformY( y ) );
	}

	protected void arc( double cx, double cy, double rx, double ry, double start, double extent ) {
		getGraphicsContext2D().arc( xformX( cx ), xformY( cy ), xformX( rx ), xformY( ry ), start, extent );
	}

	protected void arcTo( double x1, double y1, double x2, double y2, double radius ) {
		getGraphicsContext2D().arcTo( xformX( x1 ), xformY( y1 ), xformX( x2 ), xformY( y2 ), xform( radius ) );
	}

	protected void curveTo( double xc, double yc, double x1, double y1 ) {
		getGraphicsContext2D().quadraticCurveTo( xformX( xc ), xformY( yc ), xformX( x1 ), xformY( y1 ) );
	}

	protected void curveTo( double xc1, double yc1, double xc2, double yc2, double x1, double y1 ) {
		getGraphicsContext2D().bezierCurveTo( xformX( xc1 ), xformY( yc1 ), xformX( xc2 ), xformY( yc2 ), xformX( x1 ), xformY( y1 ) );
	}

	protected void closePath() {
		getGraphicsContext2D().closePath();
	}

	protected void setLineWidth( double width ) {
		getGraphicsContext2D().setLineWidth( xformX( width ) );
	}

	protected void setLineCap( StrokeLineCap cap ) {
		getGraphicsContext2D().setLineCap( cap );
	}

	protected void setLineJoin( StrokeLineJoin join ) {
		getGraphicsContext2D().setLineJoin( join );
	}

	protected void setDrawPaint( Paint paint ) {
		getGraphicsContext2D().setStroke( paint );
	}

	protected void setFillPaint( Paint paint ) {
		getGraphicsContext2D().setFill( paint );
	}

	protected void setFillRule( FillRule rule ) {
		getGraphicsContext2D().setFillRule( FillRule.EVEN_ODD );
	}

	protected void fillAndDraw() {
		fill();
		draw();
	}

	protected void fillAndDraw( Paint fillPaint ) {
		fill( fillPaint );
		draw();
	}

	protected void fillAndDraw( GradientShade shade ) {
		fillAndDraw( getIconFillPaint( shade ) );
	}

	protected void fillAndDraw( Paint fillPaint, Paint drawPaint ) {
		fill( fillPaint );
		draw( drawPaint );
	}

	protected void fillAndDraw( GradientShade shade, Paint drawPaint ) {
		fill( getIconFillPaint( shade ) );
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

	protected void fill( GradientShade shade ) {
		fill( getIconFillPaint( shade ) );
	}

	protected void draw() {
		getGraphicsContext2D().stroke();
	}

	protected void draw( Paint paint ) {
		getGraphicsContext2D().setStroke( paint );
		getGraphicsContext2D().stroke();
		getGraphicsContext2D().setStroke( getIconDrawPaint() );
	}

	protected void drawDot( double x, double y ) {
		double offset = 0.5 * getIconDrawWidth();
		drawOval( x - offset, y - offset, offset * 2, offset * 2 );
	}

	protected void fillOval( double x, double y, double w, double h ) {
		getGraphicsContext2D().fillOval( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
	}

	protected void fillCenteredOval( double cx, double cy, double rx, double ry ) {
		double x = cx - rx;
		double y = cy - ry;
		double w = rx * 2;
		double h = ry * 2;
		getGraphicsContext2D().fillOval( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
	}

	protected void drawLine( double x1, double y1, double x2, double y2 ) {
		getGraphicsContext2D().strokeLine( xformX( x1 ), xformY( y1 ), xformX( x2 ), xformY( y2 ) );
	}

	protected void drawOval( double x, double y, double w, double h ) {
		getGraphicsContext2D().strokeOval( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
	}

	protected void drawCenteredOval( double cx, double cy, double rx, double ry ) {
		double x = cx - rx;
		double y = cy - ry;
		double w = rx * 2;
		double h = ry * 2;
		getGraphicsContext2D().strokeOval( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
	}

	protected void clearRect( double x, double y, double w, double h ) {
		getGraphicsContext2D().clearRect( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
	}

	protected double xform( double value ) {
		return size * value;
	}

	protected double xformX( double value ) {
		return size * (value + xOffset);
	}

	protected double xformY( double value ) {
		return size * (value + yOffset);
	}

	protected double g( double value, double grid ) {
		return value / grid;
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

	protected Color getIconFillColor() {
		return fillColor;
	}

	protected Paint getIconFillPaint() {
		return getIconFillPaint( GradientShade.MEDIUM );
	}

	protected Paint getIconFillPaint( GradientShade shade ) {
		double a = 1;
		double b = 0;

		switch( shade ) {
			case LIGHT: {
				a = 0.9;
				b = 0.7;
				break;
			}
			case MEDIUM: {
				a = 0.8;
				b = 0.2;
				break;
			}
			case DARK: {
				a = 0.3;
				b = 0.1;
				break;
			}
		}

		Color colorA = Colors.getShade( getIconFillColor(), a );
		Color colorB = Colors.getShade( getIconFillColor(), b );

		return getGradientPaint( colorA, colorB );
	}

	protected static void proof( ProgramIcon icon ) {
		Platform.startup( () -> {
			String title = icon.getClass().getSimpleName();

			ImageView imageView16 = new ImageView( resample( icon.clone().setSize( 16 ).getImage(), 16 ) );
			ImageView imageView32 = new ImageView( resample( icon.clone().setSize( 32 ).getImage(), 8 ) );

			GridPane pane = new GridPane();
			pane.add( icon, 1, 1 );
			pane.add( imageView16, 2, 1 );
			pane.add( imageView32, 2, 2 );

			Stage stage = new Stage();
			stage.setTitle( title );
			stage.setScene( new Scene( pane ) );

			stage.setResizable( false );
			//stage.centerOnScreen();
			//stage.sizeToScene();
			stage.show();
		} );
	}

	private static Image resample( Image input, int scale ) {
		final int W = (int)input.getWidth();
		final int H = (int)input.getHeight();
		final int S = scale;

		WritableImage output = new WritableImage( W * S, H * S );

		PixelReader reader = input.getPixelReader();
		PixelWriter writer = output.getPixelWriter();

		for( int y = 0; y < H; y++ ) {
			for( int x = 0; x < W; x++ ) {
				final int argb = reader.getArgb( x, y );
				for( int dy = 0; dy < S; dy++ ) {
					for( int dx = 0; dx < S; dx++ ) {
						writer.setArgb( x * S + dx, y * S + dy, argb );
					}
				}
			}
		}

		return output;
	}

	private Paint getGradientPaint( Color a, Color b ) {
		return new LinearGradient( 0, 0, xformX( 1 ), xformX( 1 ), false, CycleMethod.NO_CYCLE, new Stop( 0, a ), new Stop( 1, b ) );
	}

	private void fireRender() {
		size = Math.min( getWidth(), getHeight() );

		// Set the defaults
		setLineCap( StrokeLineCap.ROUND );
		setLineJoin( StrokeLineJoin.ROUND );
		setLineWidth( getIconDrawWidth() );
		setDrawPaint( getIconDrawPaint() );
		setFillPaint( getIconFillPaint( GradientShade.MEDIUM ) );
		setFillRule( FillRule.EVEN_ODD );

		// Start rendering by clearing the icon area
		clearRect( 0, 0, 1, 1 );

		//		// Just for research, set different color backgrounds per xformX
		//		double xformX = Math.min( getWidth(), getHeight() );
		//		if( xformX == 16 ) protected void setFillColor( Color.PURPLE );
		//		if( xformX == 24 ) protected void setFillColor( Color.BLUE );
		//		if( xformX == 32 ) protected void setFillColor( Color.GREEN );
		//		if( xformX == 64 ) protected void setFillColor( Color.YELLOW );
		//		if( xformX == 128 ) protected void setFillColor( Color.ORANGE );
		//		if( xformX == 256 ) protected void setFillColor( Color.RED );
		//		protected void fillRect( 0, 0, getWidth(), getHeight() );

		render();
	}

}
