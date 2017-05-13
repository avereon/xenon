package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class CopyIcon extends ProgramIcon {

	@Override
	protected void render() {
		renderPage();
		//setColorMode( ColorMode.SECONDARYA );
		fillAndDraw();

		move( g( 8 ), g( 4 ) );

		renderPage();
		//setColorMode( ColorMode.PRIMARY );
		fillAndDraw( GradientShade.LIGHT );

		reset();
	}

	private void renderPage() {
		beginPath();
		moveTo( g( 5 ), g( 5 ) );
		lineTo( g( 5 ), g( 23 ) );
		lineTo( g( 19 ), g( 23 ) );
		lineTo( g( 19 ), g( 5 ) );
		closePath();
	}

	public static void main( String[] commands ) {
		proof( new CopyIcon() );
	}

}
