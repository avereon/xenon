package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class CutIcon extends ProgramIcon {

	@Override
	protected void render() {
		double a = 0.2265625;
		double b = 0.296875;
		double c = 0.578125;
		double d = 0.703125;
		double r = 0.5 * F;

		// Left scissor
		beginPath();
		moveTo( ZF, ZB );
		curveTo( ZF, a, G, ZG, M, ZI );
		curveTo( C, ZK, ZH, ZO, ZK, ZO );
		curveTo( ZL, ZO, ZM, ZN, ZM, ZM );
		curveTo( ZM, ZK, ZJ, ZJ, N, ZI );
		lineTo( ZF, ZB );
		moveTo( c+F, d + r );
		arc( c + r, d + r, r, r, 0, 360 );
		closePath();
		fillAndDraw();

		// Right scissor
		beginPath();
		moveTo( ZK, ZB );
		lineTo( M, ZI );
		curveTo( ZG, ZJ, ZD, ZK, ZD, ZM );
		curveTo( ZD, ZN, ZE, ZO, ZF, ZO );
		curveTo( ZI, ZO, C, ZK, N, ZI );
		curveTo( H, ZG, ZK, a, ZK, ZB );
		lineTo( ZK,ZB );
		moveTo(b+F,d+r);
		arc(b+r,d+r,r,r,0,360);
		closePath();
		fillAndDraw( GradientShade.LIGHT);

		// Hinge
		drawDot(C, ZI );
	}

	public static void main( String[] commands ) {
		proof( new CutIcon() );
	}

}
