package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import com.xeomar.xenon.JavaFxStarter;

public class WelcomeIcon extends ProgramIcon {

	@Override
	protected void render() {
		fillOval( g( 5 ), g( 5 ), g( 22 ), g( 22 ) );
		drawOval( g( 5 ), g( 5 ), g( 22 ), g( 22 ) );
	}

	public static void main( String[] commands ) {
		JavaFxStarter.startAndWait( 1000 );
		proof( new WelcomeIcon() );
	}

}
