package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class LightningBoltIcon extends ProgramIcon {


	@Override
	protected void render() {

		// Bolt
		startPath();
		moveTo( g( 16 ), g( 1 ) );
		lineTo( g( 11 ), g( 19 ) );
		lineTo( g( 16 ), g( 19 ) );
		lineTo( g( 16 ), g( 31 ) );
		lineTo( g( 21 ), g( 13 ) );
		lineTo( g( 16 ), g( 13 ) );
		closePath();
		fill( GradientTone.LIGHT );
		draw();
	}

	public static void main( String[] commands ) {
		proof( new LightningBoltIcon() );
	}



}
