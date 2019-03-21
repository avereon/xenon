package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class DeleteIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		addArc( g( 16 ), g( 24 ), g( 9 ), g( 3 ), 180, 180 );
		lineTo( g( 25 ), g( 8 ) );
		lineTo( g( 7 ), g( 8 ) );
		closePath();
		fillAndDraw( GradientTone.LIGHT );

		beginPath();
		addArc( g( 16 ), g( 8 ), g( 9 ), g( 3 ), 0, 360 );
		fillAndDraw( GradientTone.DARK );
	}

	public static void main( String[] commands ) {
		proof( new DeleteIcon() );
	}

}
