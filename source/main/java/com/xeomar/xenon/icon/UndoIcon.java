package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class UndoIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( g( 15 ), g( 7 ) );
		lineTo( g( 3 ), g( 13 ) );
		lineTo( g( 15 ), g( 19 ) );
		closePath();
		fillAndDraw( GradientTone.LIGHT );

		beginPath();
		addArc( g( 15 ), g( 19 ), g( 14 ), g( 8 ), 90, -180 );
		addArc( g( 15 ), g( 21 ), g( 10 ), g( 6 ), 270, 180 );
		closePath();

		fillAndDraw( GradientTone.MEDIUM );
	}

	public static void main( String[] commands ) {
		proof( new UndoIcon() );
	}

}
