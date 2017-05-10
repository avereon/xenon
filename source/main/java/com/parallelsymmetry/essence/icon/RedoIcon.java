package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class RedoIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( ZI, ZD );
		lineTo( ZO, ZG );
		lineTo( ZI, ZJ );
		closePath();
		fillAndDraw( GradientShade.LIGHT );

		beginPath();
		arc( ZI, ZJ, M, D, 90, 180 );
		arc( ZI, ZK, L, K, 270, -180 );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new RedoIcon() );
	}

}
