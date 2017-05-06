package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class QuestionIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Squiggle
		beginPath();
		moveTo( ZE, ZD );
		curveTo( ZE, ZF, G, ZF );
		curveTo( ZH, ZF, ZH, ZE );

		curveTo( ZH, D, C, D );
		curveTo( ZI, D, ZI, ZE );
		curveTo( ZI, ZG, ZG, ZF, ZG, ZI );

		curveTo( ZG, ZJ, C, ZJ );
		curveTo( ZJ, ZJ, ZJ, ZI );

		curveTo( ZJ, ZG, ZL, ZG, ZL, ZE );

		curveTo( ZL, ZB, C, ZB );
		curveTo( ZE, ZB, ZE, ZD );

		closePath();
		fillAndDraw();

		// Dot
		fillOval( ZG, ZL, K, K );
		drawOval( ZG, ZL, K, K );
	}

}
