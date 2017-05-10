package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class QuestionIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Squiggle
		beginPath();
		moveTo( ZE, ZE );
		arc( G, ZE, ZB, J, 180, 180 );
		arc( C, ZE, ZA, ZA, 180, -180 );
		curveTo( ZI, ZG, ZG, ZF, ZG, ZI );
		arc( C, ZI, ZB, J, 180, 180 );
		curveTo( ZJ, ZG, ZL, ZG, ZL, ZE );
		arc( C, ZE, ZD, K, 0, 180 );
		closePath();
		fillAndDraw();

		// Dot
		fillOval( ZG, ZL, K, K );
		drawOval( ZG, ZL, K, K );
	}

	public static void main( String[] commands ) {
		proof( new QuestionIcon() );
	}

}
