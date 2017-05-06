package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class ExclamationIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Bar
		beginPath();
		moveTo( C, ZB );
		curveTo( ZG, ZB, ZG, K, ZG, ZE );
		curveTo( ZG, ZG, ZG, ZJ, C, ZJ );
		curveTo( ZJ, ZJ, ZJ, ZG, ZJ, ZE );
		curveTo( ZJ, K, ZJ, ZB, C, ZB );
		closePath();
		fillAndDraw();

		// Dot
		fillOval( ZG, ZL, K, K );
		drawOval( ZG, ZL, K, K );
	}

}
