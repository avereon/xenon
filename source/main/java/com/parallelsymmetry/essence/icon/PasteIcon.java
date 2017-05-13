package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class PasteIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Board
		beginPath();
		arc( g( 7 ), g( 25 ), g( 2 ), g( 2 ), 180, 90 );
		lineTo( g( 25 ), g( 27 ) );
		arc( g( 25 ), g( 25 ), g( 2 ), g( 2 ), 270, 90 );
		lineTo( g( 27 ), g( 7 ) );
		arc( g( 25 ), g( 7 ), g( 2 ), g( 2 ), 0, 90 );
		lineTo( g( 7 ), g( 5 ) );
		arc( g( 7 ), g( 7 ), g( 2 ), g( 2 ), 90, 90 );
		closePath();
		fillAndDraw();

		// Page
		beginPath();
		moveTo( g( 13 ), g( 9 ) );
		lineTo( g( 13 ), g( 27 ) );
		lineTo( g( 27 ), g( 27 ) );
		lineTo( g( 27 ), g( 9 ) );
		closePath();
		fillAndDraw( GradientShade.LIGHT );

		// Clip
		beginPath();
		moveTo( g( 9 ), g( 9 ) );
		lineTo( g( 9 ), g( 11 ) );
		lineTo( g( 23 ), g( 11 ) );
		lineTo( g( 23 ), g( 9 ) );
		arc( g( 16 ), g( 7 ), g( 7 ), g( 6 ), 0, 180 );
		lineTo( g( 9 ), g( 9 ) );
		// Hole
		moveTo( g( 13 ), g( 7 ) );
		lineTo( g( 19 ), g( 7 ) );
		arc( g( 16 ), g( 7 ), g( 3 ), g( 2 ), 0, 180 );
		closePath();
		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new PasteIcon() );
	}

}
