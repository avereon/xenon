package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class LightningIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( g( 15 ), g( 5 ) );
		lineTo( g( 11 ), g( 17 ) );
		lineTo( g( 15 ), g( 17 ) );
		lineTo( g( 9 ), g( 29 ) );
		lineTo( g( 21 ), g( 13 ) );
		lineTo( g( 17 ), g( 13 ) );
		lineTo( g( 21 ), g( 5 ) );
		closePath();
		fill();
		draw();
	}

	public static void main( String[] commands ) {
		proof( new LightningIcon() );
	}

}
