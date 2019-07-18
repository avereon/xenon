package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class IndentIcon extends ProgramIcon {

	@Override
	protected void render() {
		startPath();
		moveTo( g( 7 ), g( 11 ) );
		lineTo( g( 12 ), g( 16 ) );
		lineTo( g( 7 ), g( 21 ) );
		closePath();
		fillAndDraw();

		drawLine( g( 7 ), g( 7 ), g( 25 ), g( 7 ) );
		drawLine( g( 15 ), g( 13 ), g( 25 ), g( 13 ) );
		drawLine( g( 15 ), g( 19 ), g( 25 ), g( 19 ) );
		drawLine( g( 7 ), g( 25 ), g( 25 ), g( 25 ) );
	}

	public static void main( String[] commands ) {
		proof( new IndentIcon() );
	}

}
