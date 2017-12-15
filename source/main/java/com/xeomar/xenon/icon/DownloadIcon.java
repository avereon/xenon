package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class DownloadIcon extends ProgramIcon {

	@Override
	protected void render() {
		double ox = g( 16 );
		double oy = g( 23 );

		double m = g( 18 );

		double shaft = g( 3 );
		double width = g( 9 );

		double za = g( 1 );

		fillCenteredOval( ox, oy, g( 10 ), g( 4 ) );
		drawCenteredOval( ox, oy, g( 10 ), g( 4 ) );

		beginPath();
		moveTo( ox - za, oy );
		lineTo( ox - width, oy - width - za );
		lineTo( ox - shaft, oy - width - za );
		lineTo( ox - shaft, oy - m );

		lineTo( ox + shaft, oy - m );
		lineTo( ox + shaft, oy - width - za );
		lineTo( ox + width, oy - width - za );
		lineTo( ox + za, oy );
		closePath();
		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new DownloadIcon() );
	}

}
