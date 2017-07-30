package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class ExclamationIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Bar
		beginPath();
		moveTo( g( 16 ), g( 3 ) );
		curveTo( g( 13 ), g( 3 ), g( 13 ), g( 6 ), g( 13 ), g( 9 ) );
		curveTo( g( 13 ), g( 13 ), g( 13 ), g( 19 ), g( 16 ), g( 19 ) );
		curveTo( g( 19 ), g( 19 ), g( 19 ), g( 13 ), g( 19 ), g( 9 ) );
		curveTo( g( 19 ), g( 6 ), g( 19 ), g( 3 ), g( 16 ), g( 3 ) );
		closePath();
		fillAndDraw();

		// Dot
		fillOval( g( 13 ), g( 23 ), g( 6 ), g( 6 ) );
		drawOval( g( 13 ), g( 23 ), g( 6 ), g( 6 ) );
	}

	public static void main( String[] commands ) {
		proof( new ExclamationIcon() );
	}

}
