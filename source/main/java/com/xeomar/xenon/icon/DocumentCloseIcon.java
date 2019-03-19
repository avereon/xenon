package com.xeomar.xenon.icon;

public class DocumentCloseIcon extends DocumentIcon {

	public void render() {
		super.render();

		beginPath();
		moveTo( g( 11 ), g( 15 ) );
		lineTo( g( 21 ), g( 25 ) );
		moveTo( g( 21 ), g( 15 ) );
		lineTo( g( 11 ), g( 25 ) );

		setLineWidth( g( 2 ) );
		draw();
	}

	public static void main( String[] commands ) {
		proof( new DocumentCloseIcon() );
	}

}
