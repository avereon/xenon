package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class UnindentIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( g( 11 ), g( 11 ) );
		lineTo( g( 6 ), g( 16 ) );
		lineTo( g( 11 ), g( 21 ) );
		closePath();
		fillAndDraw();

		drawLine( g( 7 ), g( 7 ), g( 25 ), g( 7 ) );
		drawLine( g( 15 ), g( 13 ), g( 25 ), g( 13 ) );
		drawLine( g( 15 ), g( 19 ), g( 25 ), g( 19 ) );
		drawLine( g( 7 ), g( 25 ), g( 25 ), g( 25 ) );
	}

	public static void main( String[] commands ) {
		proof( new UnindentIcon() );
	}

}
