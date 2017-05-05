package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.OldProgramIcon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ExclamationIcon extends OldProgramIcon {

	public ExclamationIcon() {
	}

	@Override
	protected void render( GraphicsContext gfx ) {
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
		gfx.setFill( Color.CORNFLOWERBLUE );
		gfx.fillOval( scale( ZG ), scale( ZL ), scale( K ), scale( K ) );
		gfx.setFill( Color.BLUE );
	}
}
