package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import com.xeomar.xenon.util.Colors;
import com.xeomar.xenon.util.JavaFxStarter;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/*
Conversion chart

A ----- 0
    J - 2
  F --- 4
    K - 6
D ----- 8
    L - 10
  G --- 12
    M - 14
C ----- 16
    N - 18
  H --- 20
    O - 22
E ----- 24
    P - 26
  I --- 28
    Q - 30
B ----- 32

ZA - 1
ZB - 3
ZC - 5
ZD - 7
ZE - 9
ZF - 11
ZG - 13
ZH - 15
ZI - 17
ZJ - 19
ZK - 21
ZL - 23
ZM - 25
ZN - 27
ZO - 29
ZP - 31
*/
public abstract class ProgramIcon extends Canvas {

	private static final double DEFAULT_SIZE = 256;

	protected enum GradientShade {
		LIGHT,
		MEDIUM,
		DARK
	}

	private static Logger log = LogUtil.get( ProgramIcon.class );

	private static double DEFAULT_STROKE_WIDTH = 1.0 / 32.0;

	private static Color DEFAULT_STROKE_COLOR = new Color( 0.2, 0.2, 0.2, 1.0 );

	private static Color DEFAULT_FILL_COLOR = new Color( 0.8, 0.8, 0.8, 1.0 );

	private static double drawWidth = DEFAULT_STROKE_WIDTH;

	private static Color themeDrawColor;

	private static Color themeFillColor;

	private static Color drawColor;

	private static Color fillColor;

	private static ColorTheme theme;

	private double size;

	private double xOffset;

	private double yOffset;

	static {
		setColorTheme( new ColorTheme( new Color( 0.8, 0.8, 0.8, 1.0 ) ) );
		//setColorTheme( new ColorTheme( Color.GRAY.darker() ) );
	}

	public ProgramIcon() {
		widthProperty().addListener( ( property, oldValue, newValue ) -> fireRender() );
		heightProperty().addListener( ( property, oldValue, newValue ) -> fireRender() );
		setSize( DEFAULT_SIZE );
	}

	public static void setColorTheme( ColorTheme theme ) {
		ProgramIcon.theme = theme;
		themeFillColor = theme.getPrimary();

		// A luminance greater than 0.5 is "bright"
		// A luminance less than 0.5 is "dark"
		double y = Colors.getLuminance( theme.getPrimary() );
		if( y < 0.5 ) {
			themeDrawColor = theme.getPrimary().deriveColor( 0, 1, 1.5, 1 );
		} else {
			themeDrawColor = theme.getPrimary().deriveColor( 0, 1, 0.25, 1 );
		}
	}

	public static void setDrawColor( Color color ) {
		drawColor = color;
	}

	public static void setFillColor( Color color ) {
		fillColor = color;
	}

	public ProgramIcon setSize( double size ) {
		setHeight( size );
		setWidth( size );
		return this;
	}

	public Image getImage() {
		return SwingFXUtils.toFXImage( getBufferedImage(), new WritableImage( (int)getWidth(), (int)getHeight() ) );
	}

	public BufferedImage getBufferedImage() {
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
		return buffer;
	}

	public static void proof( ProgramIcon icon ) {
		JavaFxStarter.startAndWait( 1000 );

		// Now show the icon window
		Platform.runLater( () -> {
			String title = icon.getClass().getSimpleName();

			ImageView imageView16 = new ImageView( resample( icon.copy().setSize( 16 ).getImage(), 16 ) );
			ImageView imageView32 = new ImageView( resample( icon.copy().setSize( 32 ).getImage(), 8 ) );

			ProgramIcon icon128 = icon.copy().setSize( 128 );
			AnchorPane.setTopAnchor( icon128, 0.0 );
			AnchorPane.setLeftAnchor( icon128, 0.0 );

			ProgramIcon icon64 = icon.copy().setSize( 64 );
			AnchorPane.setTopAnchor( icon64, 128.0 );
			AnchorPane.setLeftAnchor( icon64, 128.0 );

			ProgramIcon icon32 = icon.copy().setSize( 32 );
			AnchorPane.setTopAnchor( icon32, 192.0 );
			AnchorPane.setLeftAnchor( icon32, 192.0 );

			ProgramIcon icon16 = icon.copy().setSize( 16 );
			AnchorPane.setTopAnchor( icon16, 224.0 );
			AnchorPane.setLeftAnchor( icon16, 224.0 );

			ProgramIcon icon8 = icon.copy().setSize( 8 );
			AnchorPane.setTopAnchor( icon8, 240.0 );
			AnchorPane.setLeftAnchor( icon8, 240.0 );

			AnchorPane iconPane = new AnchorPane();
			iconPane.getChildren().addAll( icon128, icon64, icon32, icon16, icon8 );

			GridPane pane = new GridPane();
			pane.add( icon.copy().setSize( DEFAULT_SIZE ), 1, 1 );
			pane.add( imageView16, 2, 1 );
			pane.add( imageView32, 2, 2 );
			pane.add( iconPane, 1, 2 );

			Stage stage = new Stage();
			stage.setTitle( title );
			stage.getIcons().addAll( icon128.copy().getImage(), icon64.copy().getImage(), icon32.copy().getImage(), icon16.copy().getImage() );
			stage.setScene( new Scene( pane ) );

			stage.setResizable( false );
			stage.centerOnScreen();
			stage.sizeToScene();
			stage.show();
		} );
	}

	public static void save( ProgramIcon icon, String path ) {
		JavaFxStarter.startAndWait( 1000 );

		// Render and save the icon
		Platform.runLater( () -> {
			try {
				File file = new File( path ).getCanonicalFile();
				ImageIO.write( icon.getBufferedImage(), "png", file );
			} catch( Exception exception ) {
				exception.printStackTrace();
			}
		} );

		Platform.exit();
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

	protected void clip() {
		getGraphicsContext2D().clip();
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

	protected void addArc( double cx, double cy, double rx, double ry, double start, double extent ) {
		getGraphicsContext2D().arc( xformX( cx ), xformY( cy ), xformX( rx ), xformY( ry ), start, extent );
	}

	protected void addRect( double x, double y, double w, double h ) {
		getGraphicsContext2D().rect( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
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
		getGraphicsContext2D().setStroke( getIconDrawColor() );
	}

	protected void drawDot( double x, double y ) {
		double offset = 0.5 * getIconDrawWidth();
		drawOval( x - offset, y - offset, offset * 2, offset * 2 );
	}

	protected void fillRect( double x, double y, double w, double h ) {
		getGraphicsContext2D().fillRect( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
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

	protected void drawRect( double x, double y, double w, double h ) {
		getGraphicsContext2D().strokeRect( xformX( x ), xformY( y ), xformX( w ), xformY( h ) );
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

	protected double xformX( double value ) {
		return size * (value + xOffset);
	}

	protected double xformY( double value ) {
		return size * (value + yOffset);
	}

	protected double g( double value ) {
		return value / 32d;
	}

	protected double getIconDrawWidth() {
		return drawWidth;
	}

	protected Color getIconDrawColor() {
		return drawColor == null ? themeDrawColor : drawColor;
	}

	protected Color getIconFillColor() {
		return fillColor == null ? themeFillColor : fillColor;
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

	protected Paint radialPaint( double x, double y, double r, Stop... stops ) {
		return new RadialGradient( 0, 0, xformX( x ), xformY( y ), xformX( r ), false, CycleMethod.NO_CYCLE, stops );
	}

	protected Paint radialPaint( double x, double y, double r, List<Stop> stops ) {
		return new RadialGradient( 0, 0, xformX( x ), xformY( y ), xformX( r ), false, CycleMethod.NO_CYCLE, stops );
	}

	protected static double distance( double x1, double y1, double x2, double y2 ) {
		return Math.sqrt( (Math.pow( y2 - y1, 2 ) + Math.pow( x2 - x1, 2 )) );
	}

	private ProgramIcon copy() {
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

	private static Image resample( Image input, int scale ) {
		int w = (int)input.getWidth();
		int h = (int)input.getHeight();

		WritableImage output = new WritableImage( w * scale, h * scale );

		PixelReader reader = input.getPixelReader();
		PixelWriter writer = output.getPixelWriter();

		for( int y = 0; y < h; y++ ) {
			for( int x = 0; x < w; x++ ) {
				final int argb = reader.getArgb( x, y );
				for( int dy = 0; dy < scale; dy++ ) {
					for( int dx = 0; dx < scale; dx++ ) {
						writer.setArgb( x * scale + dx, y * scale + dy, argb );
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
		setDrawPaint( getIconDrawColor() );
		setFillPaint( getIconFillPaint( GradientShade.MEDIUM ) );
		setFillRule( FillRule.EVEN_ODD );

		// Start rendering by clearing the icon area
		clearRect( 0, 0, 1, 1 );

		//		// Just for research, set different color backgrounds per size
		//		if( size == 16 ) protected void setFillColor( Color.PURPLE );
		//		if( size == 24 ) protected void setFillColor( Color.BLUE );
		//		if( size == 32 ) protected void setFillColor( Color.GREEN );
		//		if( size == 64 ) protected void setFillColor( Color.YELLOW );
		//		if( size == 128 ) protected void setFillColor( Color.ORANGE );
		//		if( size == 256 ) protected void setFillColor( Color.RED );
		//		protected void fillRect( 0, 0, getWidth(), getHeight() );

		render();
	}

}
