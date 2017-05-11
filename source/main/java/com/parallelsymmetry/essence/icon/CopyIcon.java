package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class CopyIcon extends ProgramIcon {

	@Override
	protected void render() {
		renderPage();
		//setColorMode( ColorMode.SECONDARYA );
		fillAndDraw( GradientShade.DARK );

		move( D, F );

		renderPage();
		//setColorMode( ColorMode.PRIMARY );
		fillAndDraw();

		reset();
	}

	private void renderPage() {
		beginPath();
		moveTo( ZC, ZC );
		lineTo( ZC, ZL );
		lineTo( ZJ, ZL );
		lineTo( ZJ, ZC );
		closePath();
	}

	public static void main( String[] commands ) {
		proof( new CopyIcon() );
	}

}
