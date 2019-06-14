package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class DisableIcon extends ProgramIcon {

	@Override
	protected void render() {
		double alpha = 13;
		double theta = (45 - alpha) / 180.0 * Math.PI;
		double r1 = g( 9 );
		double r2 = g( 13 );
		double x = r1 * Math.sin( theta );
		double y = r1 * Math.cos( theta );

		startPath();
		addOval( g( 16 ), g( 16 ), r2, r2 );

		moveTo( g( 16 ) + x, g( 16 ) - y );
		addArc( g( 16 ), g( 16 ), r1, r1, 45 + alpha, 180 - 2 * alpha );
		closePath();

		moveTo( g( 16 ) - x, g( 16 ) + y );
		addArc( g( 16 ), g( 16 ), r1, r1, 225 + alpha, 180 - 2 * alpha );
		closePath();

		fill();
		draw();
	}

	public static void main( String[] commands ) {
		proof( new DisableIcon() );
	}

}
