package com.parallelsymmetry.essence;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

public class TestIcon extends Region {

	public TestIcon() {
		setWidth( 1 );
		setHeight( 1 );

		Ellipse circle = new Ellipse( 0.5, 0.5, 0.5, 0.5 );
		circle.setFill( Color.BLUE.darker() );
		getChildren().add( circle );

		setWidth( 16 );
		setHeight( 16 );
//		circle.setScaleX( 16 );
//		circle.setScaleY( 16 );
	}

	@Override
	public void setWidth( double width ) {
		super.setWidth( width );
		super.setScaleX( width );
//		for( Node child : getChildren() ) {
//			child.setScaleX( width );
//		}
	}

	@Override
	protected void setHeight( double height ) {
		super.setHeight( height );
		super.setScaleY( height );
//		for( Node child : getChildren() ) {
//			child.setScaleY( height );
//		}
	}

}
