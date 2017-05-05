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

import java.awt.image.BufferedImage;

public abstract class ProgramIcon extends StackPane {

	private Group group;

	public ProgramIcon() {
		group = new Group();
		super.getChildren().add( group );

		setIconWidth( 16 );
		setIconHeight( 16 );

		configure( group );
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
