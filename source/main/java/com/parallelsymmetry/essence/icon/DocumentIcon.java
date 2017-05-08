package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class DocumentIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( ZC, ZB );
		lineTo( ZC, ZO );
		lineTo( ZN, ZO );
		lineTo( ZN, ZF );
		lineTo( ZJ, ZB );
		closePath();
		fill();

		beginPath();
		moveTo( ZJ, ZB );
		lineTo( ZJ, ZF );
		lineTo( ZN, ZF );
		closePath();
		fill( getIconFillPaint( GradientShade.DARK ) );

		beginPath();
		moveTo( ZC, ZB );
		lineTo( ZC, ZO );
		lineTo( ZN, ZO );
		lineTo( ZN, ZF );
		lineTo( ZJ, ZB );
		closePath();
		draw();

		beginPath();
		moveTo( ZJ, ZB );
		lineTo( ZJ, ZF );
		lineTo( ZN, ZF );
		// Intentionally don't close path
		draw();
	}

	public static void main( String[] commands ) {
		proof( new DocumentIcon() );
	}

}
