package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class CloseToolIcon extends ProgramIcon {

	private final int a = 11;

	private final int b = 21;

	@Override
	protected void render() {
		setDrawWidth( 2 * getDefaultDrawWidth() );
		drawLine( g( a ), g( a ), g( b ), g( b ) );
		drawLine( g( a ), g( b ), g( b ), g( a ) );
	}

	public static void main( String[] commands ) {
		proof( new CloseToolIcon() );
	}

}
