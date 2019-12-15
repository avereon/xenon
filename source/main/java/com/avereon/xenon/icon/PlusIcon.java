package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;

public class PlusIcon extends ProgramIcon {

	@Override
	protected void render() {
		startPath();
		moveTo( g( 13 ), g( 8 ) );
		lineTo( g( 13 ), g( 13 ) );
		lineTo( g( 8 ), g( 13 ) );
		addArc( g( 8 ), g( 16 ), g( 3 ), g( 3 ), 90, 180 );
		lineTo( g( 13 ), g( 19 ) );
		lineTo( g( 13 ), g( 24 ) );
		addArc( g( 16 ), g( 24 ), g( 3 ), g( 3 ), 180, 180 );
		lineTo( g( 19 ), g( 19 ) );
		lineTo( g( 24 ), g( 19 ) );
		addArc( g( 24 ), g( 16 ), g( 3 ), g( 3 ), 270, 180 );
		lineTo( g( 19 ), g( 13 ) );
		lineTo( g( 19 ), g( 8 ) );
		addArc( g( 16 ), g( 8 ), g( 3 ), g( 3 ), 0, 180 );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new PlusIcon() );
	}

}
