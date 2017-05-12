package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class PasteIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( ZC, ZD );
		lineTo( ZC, ZM );
		lineTo( ZD, ZN );
		lineTo( ZM, ZN );
		lineTo( ZN, ZM );
		lineTo( ZN, ZD );
		lineTo( ZM, ZC );
		lineTo( ZD, ZC );
		closePath();
		fillAndDraw();

		beginPath();
		moveTo( ZG, ZE );
		lineTo( ZG, ZN );
		lineTo( ZN, ZN );
		lineTo( ZN, ZE );
		closePath();
		fillAndDraw( GradientShade.LIGHT );

		beginPath();
		moveTo( ZE, ZE );
		lineTo( ZE, ZF );
		lineTo( ZL, ZF );
		lineTo( ZL, ZE );
		arc( C, ZD, M / 2, ZF / 2, 0, 180 );
		closePath();
		fillAndDraw();

		// TODO Finish PasteIcon
		beginPath();
		moveTo( M,ZD );
		lineTo( N,ZD );
		arc(C,ZD,ZC,ZB, 0,180);
		//drawLine( M, ZD, N, ZD );
		closePath();
		fillAndDraw(GradientShade.DARK);
	}

	public static void main( String[] commands ) {
		proof( new PasteIcon() );
	}

}
