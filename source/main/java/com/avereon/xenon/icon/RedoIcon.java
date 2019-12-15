package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;

public class RedoIcon extends ProgramIcon {

	@Override
	protected void render() {
		startPath();
		moveTo( g( 17 ), g( 7 ) );
		lineTo( g( 29 ), g( 13 ) );
		lineTo( g( 17 ), g( 19 ) );
		closePath();
		fillAndDraw( GradientTone.LIGHT );

		startPath();
		addArc( g( 17 ), g( 19 ), g( 14 ), g( 8 ), 90, 180 );
		addArc( g( 17 ), g( 21 ), g( 10 ), g( 6 ), 270, -180 );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new RedoIcon() );
	}

}
