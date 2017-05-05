package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class QuestionMarkIcon extends ProgramIcon {

	@Override
	protected void render() {
		// TODO Fix the question mark icon arcs

		// Curve
		beginPath();
		moveTo( ZE, ZD );
		//sign.append( new Arc( ZE, ZD, K, F, 180, 180, Arc.OPEN ), false );
		//sign.append( new Arc( ZH, D, J, J, 180, -180, Arc.OPEN ), true );
		bezierCurveTo( ZI, ZG, ZG, ZF, ZG, ZI );
		//sign.append( new Arc( ZG, ZH, K, F, 180, 180, Arc.OPEN ), true );
		bezierCurveTo( ZJ, ZG, ZL, ZG, ZL, ZE );
		//sign.append( new Arc( ZE, ZB, M, G, 0, 180, Arc.OPEN ), true );
		closePath();
		fillAndDraw();

		// Dot
		fillOval( ZG, ZL, K, K );
		drawOval( ZG, ZL, K, K );
	}

}
