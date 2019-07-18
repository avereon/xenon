package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class GuideIcon extends ProgramIcon {

	@Override
	protected void render() {
		startPath();
		moveTo( g( 11 ), g( 9 ) );
		lineTo( g( 11 ), g( 27 ) );
		lineTo( g( 17 ), g( 27 ) );
		moveTo( g( 11 ), g( 15 ) );
		lineTo( g( 17 ), g( 15 ) );
		draw();

		fillCenteredOval( g( 11 ), g( 5 ), g( 4 ), g( 4 ) );
		fillCenteredOval( g( 21 ), g( 15 ), g( 4 ), g( 4 ) );
		fillCenteredOval( g( 21 ), g( 27 ), g( 4 ), g( 4 ) );

		drawCenteredOval( g( 11 ), g( 5 ), g( 4 ), g( 4 ) );
		drawCenteredOval( g( 21 ), g( 15 ), g( 4 ), g( 4 ) );
		drawCenteredOval( g( 21 ), g( 27 ), g( 4 ), g( 4 ) );
	}

	public static void main( String[] commands ) {
		proof( new GuideIcon() );
	}

}
