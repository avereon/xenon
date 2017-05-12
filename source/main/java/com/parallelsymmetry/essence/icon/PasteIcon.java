package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class PasteIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Board
		beginPath();
		arc( g32( 7 ), g32( 25 ), g16( 1 ), g16( 1 ), 180, 90 );
		lineTo( g32( 25 ), g32( 27 ) );
		arc( g32( 25 ), g32( 25 ), g16( 1 ), g16( 1 ), 270, 90 );
		lineTo( g32( 27 ), g32( 7 ) );
		arc( g32( 25 ), g32( 7 ), g16( 1 ), g16( 1 ), 0, 90 );
		lineTo( g32( 7 ), g32( 5 ) );
		arc( g32( 7 ), g32( 7 ), g16( 1 ), g16( 1 ), 90, 90 );
		closePath();
		fillAndDraw();

		// Page
		beginPath();
		moveTo( g32( 13 ), g32( 9 ) );
		lineTo( g32( 13 ), g32( 27 ) );
		lineTo( g32( 27 ), g32( 27 ) );
		lineTo( g32( 27 ), g32( 9 ) );
		closePath();
		fillAndDraw( GradientShade.LIGHT );

		// Clip
		beginPath();
		moveTo( g32( 9 ), g32( 9 ) );
		lineTo( g32( 9 ), g32( 11 ) );
		lineTo( g32( 23 ), g32( 11 ) );
		lineTo( g32( 23 ), g32( 9 ) );
		arc( g2( 1 ), g32( 7 ), g32( 7 ), g16( 3 ), 0, 180 );
		lineTo( g32( 9 ), g32( 9 ) );
		// Hole
		moveTo( g32( 13 ), g32( 7 ) );
		lineTo( g32( 19 ), g32( 7 ) );
		arc( g2( 1 ), g32( 7 ), g32( 3 ), g16( 1 ), 0, 180 );
		closePath();
		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new PasteIcon() );
	}

}
