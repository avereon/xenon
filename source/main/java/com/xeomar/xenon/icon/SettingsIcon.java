package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class SettingsIcon extends ProgramIcon {

	@Override
	protected void render() {
		renderSliders();
	}

	private void renderSliders() {
		int r = 4;

		int y1 = 5;
		int y2 = 15;
		int y3 = 25;

		int b1 = 11;
		int b2 = 19;
		int b3 = 9;

		drawLine( g( 3 ), g( y1 ), g( 26 ), g( y1 ) );
		drawLine( g( 3 ), g( y2 ), g( 26 ), g( y2 ) );
		drawLine( g( 3 ), g( y3 ), g( 26 ), g( y3 ) );

		fillCenteredOval( g( b1 ), g( y1 ), g( r ), g( r ) );
		drawCenteredOval( g( b1 ), g( y1 ), g( r ), g( r ) );

		fillCenteredOval( g( b2 ), g( y2 ), g( r ), g( r ) );
		drawCenteredOval( g( b2 ), g( y2 ), g( r ), g( r ) );

		fillCenteredOval( g( b3 ), g( y3 ), g( r ), g( r ) );
		drawCenteredOval( g( b3 ), g( y3 ), g( r ), g( r ) );
	}

	private void renderBars() {
		fillRect( g( 3 ), g( 7 ), g( 26 ), g( 4 ) );
		drawRect( g( 3 ), g( 7 ), g( 26 ), g( 4 ) );

		fillRect( g( 3 ), g( 15 ), g( 26 ), g( 4 ) );
		drawRect( g( 3 ), g( 15 ), g( 26 ), g( 4 ) );

		fillRect( g( 3 ), g( 23 ), g( 26 ), g( 4 ) );
		drawRect( g( 3 ), g( 23 ), g( 26 ), g( 4 ) );
	}

	private void renderBubbles() {
		fillCenteredOval( g( 16 ), g( 6 ), g( 3 ), g( 3 ) );
		drawCenteredOval( g( 16 ), g( 6 ), g( 3 ), g( 3 ) );

		fillCenteredOval( g( 16 ), g( 16 ), g( 3 ), g( 3 ) );
		drawCenteredOval( g( 16 ), g( 16 ), g( 3 ), g( 3 ) );

		fillCenteredOval( g( 16 ), g( 26 ), g( 3 ), g( 3 ) );
		drawCenteredOval( g( 16 ), g( 26 ), g( 3 ), g( 3 ) );
	}

	public static void main( String[] commands ) {
		proof( new SettingsIcon() );
	}

}
