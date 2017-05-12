package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class PasteIcon extends ProgramIcon {

	@Override
	protected void render() {
//		Path board = new Path();
//		board.moveTo( ZC, ZD );
//		board.lineTo( ZC, ZM );
//		board.lineTo( ZD, ZN );
//		board.lineTo( ZM, ZN );
//		board.lineTo( ZN, ZM );
//		board.lineTo( ZN, ZD );
//		board.lineTo( ZM, ZC );
//		board.lineTo( ZD, ZC );
//		board.closePath();
//
//		Path page = new Path();
//		page.moveTo( ZG, ZE );
//		page.lineTo( ZG, ZN );
//		page.lineTo( ZN, ZN );
//		page.lineTo( ZN, ZE );
//		page.closePath();
//
//		Path clip = new Path();
//		clip.moveTo( ZE, ZE );
//		clip.lineTo( ZE, ZF );
//		clip.lineTo( ZL, ZF );
//		clip.lineTo( ZL, ZE );
//		clip.append( new Arc( ZE, ZB, M, ZF, 0, 180, Arc.OPEN ), true );
//		clip.closePath();
//
//		Path slot = new Path();
//		slot.moveTo( M, ZD );
//		slot.lineTo( N, ZD );
//
//		fill( board, GradientType.DARK );
//		draw( board );
//
//		fill( page );
//		draw( page );
//
//		fill( clip );
//		draw( clip );
//		draw( slot );
	}

	public static void main( String[] commands ) {
		proof( new PasteIcon() );
	}

}
