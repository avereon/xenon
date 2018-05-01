package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class TaskQueueIcon extends ProgramIcon {

	@Override
	protected void render() {
//		// Left bar
//		fillRect( g( 3 ), g( 3 ), g( 4 ), g( 26 ) );
//		drawRect( g( 3 ), g( 3 ), g( 4 ), g( 26 ) );
//
//		// Right bar
//		fillRect( g( 25 ), g( 3 ), g( 4 ), g( 26 ) );
//		drawRect( g( 25 ), g( 3 ), g( 4 ), g( 26 ) );

		// Tasks
		int r = 3;
		fillCenteredOval( g( 16 ), g( 6 ), g( r ), g( r ) );
		drawCenteredOval( g( 16 ), g( 6 ), g( r ), g( r ) );

		fillCenteredOval( g( 16 ), g( 16 ), g( r ), g( r ) );
		drawCenteredOval( g( 16 ), g( 16 ), g( r ), g( r ) );

		fillCenteredOval( g( 16 ), g( 26 ), g( r ), g( r ) );
		drawCenteredOval( g( 16 ), g( 26 ), g( r ), g( r ) );

		// Task lines
		//		drawLine( g( 13 ), g( 7 ), g( 19 ), g( 7 ) );
		//		drawLine( g( 13 ), g( 13 ), g( 19 ), g( 13 ) );
		//		drawLine( g( 13 ), g( 19 ), g( 19 ), g( 19 ) );
		//		drawLine( g( 13 ), g( 25 ), g( 19 ), g( 25 ) );
	}

	public static void main( String[] commands ) {
		proof( new TaskQueueIcon() );
	}

}
