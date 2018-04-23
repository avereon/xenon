package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class SettingsIcon extends ProgramIcon {

	@Override
	protected void render() {
		fillOval( g( 13 ), g( 3 ), g( 6 ), g( 6 ) );
		fillOval( g( 13 ), g( 13 ), g( 6 ), g( 6 ) );
		fillOval( g( 13 ), g( 23 ), g( 6 ), g( 6 ) );

		drawOval( g( 13 ), g( 3 ), g( 6 ), g( 6 ) );
		drawOval( g( 13 ), g( 13 ), g( 6 ), g( 6 ) );
		drawOval( g( 13 ), g( 23 ), g( 6 ), g( 6 ) );
	}

	public static void main( String[] commands ) {
		proof( new SettingsIcon() );
	}

}
