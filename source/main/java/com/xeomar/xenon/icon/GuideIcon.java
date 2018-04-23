package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class GuideIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( g( 6 ), g( 9 ) );
		lineTo( g( 6 ), g( 26 ) );
		lineTo( g( 13 ), g( 26 ) );
		moveTo( g( 6 ), g( 16 ) );
		lineTo( g( 13 ), g( 16 ) );
		draw();

		fillCenteredOval( g( 6 ), g( 6 ), g( 3 ), g( 3 ) );
		fillCenteredOval( g( 16 ), g( 16 ), g( 3 ), g( 3 ) );
		fillCenteredOval( g( 16 ), g( 26 ), g( 3 ), g( 3 ) );

		drawCenteredOval( g( 6 ), g( 6 ), g( 3 ), g( 3 ) );
		drawCenteredOval( g( 16 ), g( 16 ), g( 3 ), g( 3 ) );
		drawCenteredOval( g( 16 ), g( 26 ), g( 3 ), g( 3 ) );

		drawLine( g( 12 ), g( 6 ), g( 28 ), g( 6 ) );
		drawLine( g( 22 ), g( 16 ), g( 28 ), g( 16 ) );
		drawLine( g( 22 ), g( 26 ), g( 28 ), g( 26 ) );
	}

	public static void main( String[] commands ) {
		proof( new GuideIcon() );
	}

}
