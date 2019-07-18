package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class ModuleIcon extends ProgramIcon {

	@Override
	protected void render() {
		setFillTone( GradientTone.DARK );
		fillCenteredOval( g( 16 ), g( 16 ), g( 13 ), g( 13 ) );
		drawCenteredOval( g( 16 ), g( 16 ), g( 13 ), g( 13 ) );

		// Outline
		startPath();
		moveTo( g( 16 ), g( 7 ) );
		lineTo( g( 24 ), g( 11 ) );
		lineTo( g( 24 ), g( 21 ) );
		lineTo( g( 16 ), g( 25 ) );
		lineTo( g( 8 ), g( 21 ) );
		lineTo( g( 8 ), g( 11 ) );
		closePath();
		fill( GradientTone.MEDIUM );
		draw();

		// Edges
		startPath();
		moveTo( g( 16 ), g( 25 ) );
		lineTo( g( 16 ), g( 15 ) );
		moveTo( g( 8 ), g( 11 ) );
		lineTo( g( 16 ), g( 15 ) );
		moveTo( g( 24 ), g( 11 ) );
		lineTo( g( 16 ), g( 15 ) );
		draw();
	}

	public static void main( String[] commands ) {
		proof( new ModuleIcon() );
	}

}
