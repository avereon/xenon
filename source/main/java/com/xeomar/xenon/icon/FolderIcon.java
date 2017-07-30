package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class FolderIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Back
		beginPath();
		moveTo( g(7), g(7) );
		lineTo( g(5), g(9) );
		lineTo( g(5), g(25) );
		lineTo( g(27), g(25) );
		lineTo( g(27), g(11) );
		lineTo( g(25), g(9) );
		lineTo( g(15), g(9) );
		lineTo( g(13), g(7) );
		closePath();
		fillAndDraw();

		// Front
		beginPath();
		moveTo( g(3), g(15) );
		lineTo( g(5), g(25) );
		lineTo( g(27), g(25) );
		lineTo( g(25), g(15) );
		closePath();
		fillAndDraw( getIconFillPaint( GradientShade.DARK ) );
	}

	public static void main( String[] commands ) {
		proof( new FolderIcon() );
	}

}
