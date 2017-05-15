package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class WelcomeIcon extends ProgramIcon {

	@Override
	protected void render() {
		fillOval( g( 5 ), g( 5 ), g( 22 ), g( 22 ) );
		drawOval( g( 5 ), g( 5 ), g( 22 ), g( 22 ) );
	}

	public static void main( String[] commands ) {
		proof( new WelcomeIcon() );
	}

}
