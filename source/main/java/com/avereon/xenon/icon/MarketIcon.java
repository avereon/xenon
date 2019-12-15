package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;

public class MarketIcon extends ProgramIcon {

	@Override
	protected void render() {
		startPath();
		moveTo( g( 5 ), g( 9 ) );
		lineTo( g( 5 ), g( 25 ) );
		addArc( g( 7 ), g( 25 ), g( 2 ), g( 2 ), 180, 90 );
		lineTo( g( 25 ), g( 27 ) );
		addArc( g( 25 ), g( 25 ), g( 2 ), g( 2 ), 270, 90 );
		lineTo( g( 27 ), g( 9 ) );
		closePath();
		fill();
		draw();

		startPath();
		moveTo( g( 11 ), g( 9 ) );
		lineTo( g( 11 ), g( 7 ) );
		addArc( g( 16 ), g( 7 ), g( 5 ), g( 4 ), 180, -180 );
		lineTo( g( 21 ), g( 9 ) );
		draw();
	}

	public static void main( String[] commands ) {
		proof( new MarketIcon() );
	}

}
