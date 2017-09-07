package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class SettingIcon extends ProgramIcon {

	@Override
	protected void render() {
		//		beginPath();
		//		addArc( g( 24 ), g( 16 ), g( 3 ), g( 3 ), 270, 180 );
		//		lineTo( g( 8 ), g( 13 ) );
		//		addArc( g( 8 ), g( 16 ), g( 3 ), g( 3 ), 90, 180 );
		//		closePath();
		//		fillAndDraw();

		beginPath();
		addArc( g( 24 ), g( 16 ), g( 5 ), g( 5 ), 270, 180 );
		lineTo( g( 8 ), g( 11 ) );
		addArc( g( 8 ), g( 16 ), g( 5 ), g( 5 ), 90, 180 );
		closePath();
		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new SettingIcon() );
	}

}
