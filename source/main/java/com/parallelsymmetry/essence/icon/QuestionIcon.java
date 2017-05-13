package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class QuestionIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Squiggle
		beginPath();
		moveTo( g( 9 ), g( 9 ) );
		arc( g( 12 ), g( 9 ), g( 3 ), g( 2 ), 180, 180 );
		arc( g( 16 ), g( 9 ), g( 1 ), g( 1 ), 180, -180 );
		curveTo( g( 17 ), g( 13 ), g( 13 ), g( 11 ), g( 13 ), g( 17 ) );
		arc( g( 16 ), g( 17 ), g( 3 ), g( 2 ), 180, 180 );

		curveTo( g( 19 ), g( 13 ), g( 23 ), g( 13 ), g( 23 ), g( 9 ) );
		arc( g( 16 ), g( 9 ), g( 7 ), g( 6 ), 0, 180 );
		closePath();
		fillAndDraw();

		// Dot
		fillOval( g( 13 ), g( 23 ), g( 6 ), g( 6 ) );
		drawOval( g( 13 ), g( 23 ), g( 6 ), g( 6 ) );
	}

	public static void main( String[] commands ) {
		proof( new QuestionIcon() );
	}

}
