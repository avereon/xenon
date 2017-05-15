package com.parallelsymmetry.essence.icon;

public class WorkareaCloseIcon extends WorkareaIcon {

	@Override
	protected void render() {
		super.render();

		drawLine( g( 9 ), g( 9 ), g( 23 ), g( 23 ) );
		drawLine( g( 9 ), g( 23 ), g( 23 ), g( 9 ) );
	}

	public static void main( String[] commands ) {
		proof( new WorkareaCloseIcon() );
	}

}
