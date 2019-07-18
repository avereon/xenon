package com.avereon.xenon.icon;

public class DocumentCloseIcon extends DocumentIcon {

	public void render() {
		super.render();

		startPath();
		moveTo( g( 11 ), g( 15 ) );
		lineTo( g( 21 ), g( 25 ) );
		moveTo( g( 21 ), g( 15 ) );
		lineTo( g( 11 ), g( 25 ) );

		setDrawWidth( g( 2 ) );
		draw();
	}

	public static void main( String[] commands ) {
		proof( new DocumentCloseIcon() );
	}

}
