package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class SettingIcon  extends ProgramIcon {

	@Override
	protected void render() {
		fillOval( g( 5 ), g( 11 ), g( 22 ), g( 10 ) );

		drawOval( g( 5 ), g( 11 ), g( 22 ), g( 10 ) );
	}

	public static void main( String[] commands ) {
		proof( new SettingIcon() );
	}


}
