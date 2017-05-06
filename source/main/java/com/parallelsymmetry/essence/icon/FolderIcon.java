package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

/**
 * Created by ecco on 5/5/17.
 */
public class FolderIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Back
		beginPath();
		moveTo( ZD, ZD );
		lineTo( ZC, ZE );
		lineTo( ZC, ZM );
		lineTo( ZN, ZM );
		lineTo( ZN, ZF );
		lineTo( ZM, ZE );
		lineTo( ZH, ZE );
		lineTo( ZG, ZD );
		closePath();
		fillAndDraw();

		// Front
		beginPath();
		moveTo( ZB, ZH );
		lineTo( ZC, ZM );
		lineTo( ZN, ZM );
		lineTo( ZM, ZH );
		closePath();
		fillAndDraw( getIconFillPaint( GradientShade.DARK ) );
	}

}
