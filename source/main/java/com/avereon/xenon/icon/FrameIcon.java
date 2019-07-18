package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class FrameIcon extends ProgramIcon {

	@Override
	protected void render() {
		setFillTone( GradientTone.DARK );
		fillRect( g( 3 ), g( 5 ), g( 26 ), g( 4 ) );
		setFillTone( GradientTone.LIGHT );
		fillRect( g( 3 ), g( 9 ), g( 26 ), g( 18 ) );

		startPath();
		moveTo( g( 3 ), g( 5 ) );
		lineTo( g( 3 ), g( 27 ) );
		lineTo( g( 29 ), g( 27 ) );
		lineTo( g( 29 ), g( 5 ) );
		closePath();
		moveTo( g( 3 ), g( 9 ) );
		lineTo( g( 29 ), g( 9 ) );
		draw();
	}

	public static void main( String[] commands ) {
		proof( new FrameIcon() );
	}

}
