package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class FolderIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Back
		startPath();
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
		startPath();
		moveTo( g(3), g(15) );
		lineTo( g(5), g(25) );
		lineTo( g(27), g(25) );
		lineTo( g(25), g(15) );
		closePath();
		fillAndDraw( getIconFillPaint( GradientTone.DARK ) );
	}

	public static void main( String[] commands ) {
		proof( new FolderIcon() );
	}

}
