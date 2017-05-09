package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class SaveIcon extends ProgramIcon {

	@Override
	protected void render() {
		double ox = g( 1, 2 );
		double oy = g( 23, 32 );

		double m = g( 9, 16 );

		double shaft = g( 3, 32 );
		double width = g( 9, 32 );

		// Disk
		//setFillPaint( getIconFillPaint( GradientShade.DARK ) );
		fillCenteredOval( ox, oy, g( 5, 16 ), g( 3, 16 ) );
		drawCenteredOval( ox, oy, g( 5, 16 ), g( 3, 16 ) );

		// Arrow
		beginPath();
		moveTo( ox, oy );
		lineTo( ox - width, oy - width - g( 1, 32 ) );
		lineTo( ox - shaft, oy - width - g( 1, 32 ) );
		lineTo( ox - shaft, oy - m );
		lineTo( ox + shaft, oy - m );
		lineTo( ox + shaft, oy - width - g( 1, 32 ) );
		lineTo( ox + width, oy - width - g( 1, 32 ) );
		setFillPaint( getIconFillPaint( GradientShade.LIGHT ) );
		closePath();
		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new SaveIcon() );
	}

}
