package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class DocumentIcon extends ProgramIcon {

	@Override
	protected void render() {
		// Page
		page();
		fill();

		// Fold
		fold( true );
		fill( getIconFillPaint( GradientTone.DARK ) );

		// Page outline
		page();
		draw();

		// Fold outline
		fold( false );
		draw();
	}

	private void page() {
		beginPath();
		moveTo( g( 5 ), g( 3 ) );
		lineTo( g( 5 ), g( 29 ) );
		lineTo( g( 27 ), g( 29 ) );
		lineTo( g( 27 ), g( 11 ) );
		lineTo( g( 19 ), g( 3 ) );
		closePath();
	}

	private void fold( boolean close ) {
		beginPath();
		moveTo( g( 19 ), g( 3 ) );
		lineTo( g( 19 ), g( 11 ) );
		lineTo( g( 27 ), g( 11 ) );
		if( close ) closePath();
	}

	public static void main( String[] commands ) {
		proof( new DocumentIcon() );
	}

}
