package com.avereon.xenon.icon;

import com.avereon.venza.image.ProgramIcon;

public class WelcomeIcon extends ProgramIcon {

	@Override
	protected void render() {
		fillRect( g( 3 ), g( 5 ), g( 26 ), g( 22 ) );
		drawRect( g( 3 ), g( 5 ), g( 26 ), g( 22 ) );
	}

	public static void main( String[] commands ) {
		proof( new WelcomeIcon() );
	}

}
