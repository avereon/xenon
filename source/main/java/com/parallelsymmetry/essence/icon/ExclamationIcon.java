package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.Group;
import javafx.scene.shape.*;

public class ExclamationIcon extends ProgramIcon {

	@Override
	protected void configure( Group group ) {
		// FIXME Why does the path move the icon position
		Path line = new Path();
		line.getElements().add( new MoveTo( C, ZB ) );
		line.getElements().add( new CubicCurveTo( ZG, ZB, ZG, K, ZG, ZE ) );
		line.getElements().add( new CubicCurveTo( ZG, ZG, ZG, ZJ, C, ZJ ) );
		line.getElements().add( new CubicCurveTo( ZJ, ZJ, ZJ, ZG, ZJ, ZE ) );
		line.getElements().add( new CubicCurveTo( ZJ, K, ZJ, ZB, C, ZB ) );
		line.getElements().add( new ClosePath() );

		line.setStrokeWidth( getStrokeWidth() );
		line.setStroke( getStrokeColor() );
		line.setFill( getFillColor() );

		Ellipse dot = new Ellipse( C, ZL + (K / 2), K / 2, K / 2 );
		dot.setStrokeWidth( getStrokeWidth() );
		dot.setStroke( getStrokeColor() );
		dot.setFill( getFillColor() );

		add( getBoundingBox(), line, dot );
	}

}
