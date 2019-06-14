package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public abstract class XLargeIcon extends ProgramIcon {

	protected void xPath() {
		double offset = 5.0;
		double radius = Math.sqrt( 2 * (g( 3 ) * g( 3 )) );
		startPath();
		moveTo( g( 16 ), g( 10 ) );
		lineTo( g( 23 ), g( 3 ) );
		addArc( g( 32-offset ), g( offset ), radius, radius, 135, -180 );
		lineTo( g( 22 ), g( 16 ) );
		lineTo( g( 29 ), g( 23 ) );
		addArc( g( 32-offset ), g( 32-offset ), radius, radius, 45, -180 );
		lineTo( g( 16 ), g( 22 ) );
		lineTo( g( 9 ), g( 29 ) );
		addArc( g( offset ), g( 32-offset ), radius, radius, 315, -180 );
		lineTo( g( 10 ), g( 16 ) );
		lineTo( g( 3 ), g( 9 ) );
		addArc( g( offset ), g( offset ), radius, radius, 225, -180 );
		closePath();
	}

}
