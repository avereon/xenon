package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class UndoIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( ZH, ZD );
		lineTo( ZB, ZG );
		lineTo( ZH, ZJ );
		closePath();
		fillAndDraw();

		beginPath();
		arc( ZH, ZJ, M, D, 90, -180 );
		arc( ZH, ZK, L, K, 270, 180 );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new UndoIcon() );
	}

}
