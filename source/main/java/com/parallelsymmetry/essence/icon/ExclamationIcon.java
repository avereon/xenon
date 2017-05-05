package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class ExclamationIcon extends ProgramIcon {

	@Override
	protected void configure( Group group ) {
		Path line = new Path();
		line.getElements().add( new MoveTo( C, ZB ) );
		line.getElements().add( new CubicCurveTo( ZG, ZB, ZG, K, ZG, ZE ) );
		line.getElements().add( new CubicCurveTo( ZG, ZG, ZG, ZJ, C, ZJ ) );
		line.getElements().add( new CubicCurveTo( ZJ, ZJ, ZJ, ZG, ZJ, ZE ) );
		line.getElements().add( new CubicCurveTo( ZJ, K, ZJ, ZB, C, ZB ) );

		line.setFill( Color.CORNFLOWERBLUE );
		line.setStroke( Color.DARKGRAY );
		line.setStrokeWidth( g16( 1 ) );

		Ellipse dot = new Ellipse( C, ZL + (K / 2), K / 2, K / 2 );
		dot.setFill( Color.CORNFLOWERBLUE );
		dot.setStroke( Color.DARKGRAY );
		dot.setStrokeWidth( g16( 1 ) );

		//		Path curve = new Path();
		//		curve.moveTo( C, ZB );
		//		curve.curveTo( ZG, ZB, ZG, K, ZG, ZE );
		//		curve.curveTo( ZG, ZG, ZG, ZJ, C, ZJ );
		//		curve.curveTo( ZJ, ZJ, ZJ, ZG, ZJ, ZE );
		//		curve.curveTo( ZJ, K, ZJ, ZB, C, ZB );
		//		curve.closePath();
		//
		//		fill( curve );
		//		draw( curve );
		//
		//		Ellipse dot = new Ellipse( ZG, ZL, K, K );
		//		fill( dot );
		//		draw( dot );
		//		gfx.setFill( Color.CORNFLOWERBLUE );
		//		gfx.fillOval( scale( ZG ), scale( ZL ), scale( K ), scale( K ) );
		//		gfx.setFill( Color.BLUE );

		add( line, dot );
	}
}
