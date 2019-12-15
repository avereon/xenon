package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;

public class MinusIcon extends ProgramIcon {

	@Override
	protected void render() {
		startPath();
		moveTo( g( 24 ), g( 13 ) );
		addArc( g( 8 ), g( 16 ), g( 3 ), g( 3 ), 90, 180 );
		lineTo( g( 24 ), g( 19 ) );
		addArc( g( 24 ), g( 16 ), g( 3 ), g( 3 ), 270, 180 );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new MinusIcon() );
	}

}
