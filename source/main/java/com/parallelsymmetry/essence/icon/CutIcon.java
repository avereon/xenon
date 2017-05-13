package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class CutIcon extends ProgramIcon {

	@Override
	protected void render() {
		double a = 0.2265625;
		double b = 0.296875;
		double c = 0.578125;
		double d = 0.703125;
		double r = g( 2 );

		// Left scissor
		beginPath();
		moveTo( g( 11 ), g( 3 ) );
		curveTo( g( 11 ), a, g( 12 ), g( 13 ), g( 14 ), g( 17 ) );
		curveTo( g( 16 ), g( 21 ), g( 15 ), g( 29 ), g( 21 ), g( 29 ) );
		curveTo( g( 23 ), g( 29 ), g( 25 ), g( 27 ), g( 25 ), g( 25 ) );
		curveTo( g( 25 ), g( 21 ), g( 19 ), g( 19 ), g( 18 ), g( 17 ) );
		lineTo( g( 11 ), g( 3 ) );
		moveTo( c + g( 4 ), d + r );
		arc( c + r, d + r, r, r, 0, 360 );
		closePath();
		fillAndDraw();

		// Right scissor
		beginPath();
		moveTo( g( 21 ), g( 3 ) );
		lineTo( g( 14 ), g( 17 ) );
		curveTo( g( 13 ), g( 19 ), g( 7 ), g( 21 ), g( 7 ), g( 25 ) );
		curveTo( g( 7 ), g( 27 ), g( 9 ), g( 29 ), g( 11 ), g( 29 ) );
		curveTo( g( 17 ), g( 29 ), g( 16 ), g( 21 ), g( 18 ), g( 17 ) );
		curveTo( g( 20 ), g( 13 ), g( 21 ), a, g( 21 ), g( 3 ) );
		lineTo( g( 21 ), g( 3 ) );
		moveTo( b + g( 4 ), d + r );
		arc( b + r, d + r, r, r, 0, 360 );
		closePath();
		fillAndDraw( GradientShade.LIGHT );

		// Hinge
		drawDot( g( 16 ), g( 17 ) );
	}

	public static void main( String[] commands ) {
		proof( new CutIcon() );
	}

}
