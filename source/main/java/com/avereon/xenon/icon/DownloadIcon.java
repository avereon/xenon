package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class DownloadIcon extends ProgramIcon {

	@Override
	protected void render() {
		double ox = g( 16 );
		double oy = g( 23 );

		double m = g( 20 );

		double shaft = g( 3 );
		double width = g( 9 );

		double za = g( 1 );

		fillCenteredOval( ox, oy, g( 12 ), g( 6 ) );
		drawCenteredOval( ox, oy, g( 12 ), g( 6 ) );

		startPath();
		moveTo( ox - za, oy );
		lineTo( ox - width, oy - width - za );
		lineTo( ox - shaft, oy - width - za );
		lineTo( ox - shaft, oy - m );

		lineTo( ox + shaft, oy - m );
		lineTo( ox + shaft, oy - width - za );
		lineTo( ox + width, oy - width - za );
		lineTo( ox + za, oy );
		closePath();
		fillAndDraw( GradientTone.LIGHT );
	}

	public static void main( String[] commands ) {
		proof( new DownloadIcon() );
	}

}
