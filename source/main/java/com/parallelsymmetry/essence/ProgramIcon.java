package com.parallelsymmetry.essence;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public abstract class ProgramIcon extends StackPane {

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

	private Logger log = LoggerFactory.getLogger( ProgramIcon.class );

	private Group group;

	public ProgramIcon() {
		group = new Group();
		super.getChildren().add( group );

		setIconWidth( 16 );
		setIconHeight( 16 );

		configure( group );
	}

	public ProgramIcon clone() {
		ProgramIcon icon = null;

		try {
			icon= getClass().newInstance();
			icon.setIconWidth( getIconWidth() );
			icon.setIconHeight( getIconHeight() );
		} catch( Exception exception ) {
			log.error( "Error creating icon: " + getClass(), exception );
		}

		return icon;
	}

	public ProgramIcon setSize( double size ) {
		setIconWidth( size );
		setIconHeight( size );
		return this;
	}

	public double getIconWidth() {
		return getMinWidth();
	}

	public void setIconWidth( double width ) {
		setMinWidth( width );
		group.setScaleX( width );
	}

	public double getIconHeight() {
		return getMinHeight();
	}

	public void setIconHeight( double height ) {
		setMinHeight( height );
		group.setScaleY( height );
	}

	public Image getImage() {
		// Apparently images created from the snapshot method are not usable as
		// application icons. The following workaround creates an image that is
		// usable as an application icon. It may be more efficient to create
		// images differently if they are not needed for application icons.

		int width = (int)getIconWidth();
		int height = (int)getIconHeight();

		Pane pane = new Pane( this );
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

	protected abstract void configure( Group group );

	protected void add( Node... node ) {
		group.getChildren().addAll( node );
	}

	protected double g4( double value ) {
		return value / 4;
	}

	protected double g8( double value ) {
		return value / 8;
	}

	protected double g16( double value ) {
		return value / 16;
	}

	protected double g32( double value ) {
		return value / 32;
	}

}
