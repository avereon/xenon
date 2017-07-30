package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;

public class WorkareaRenameIcon extends WorkareaIcon {

	@Override
	protected void render() {
		super.render();

		setFillPaint( getIconFillPaint( ProgramIcon.GradientShade.LIGHT ) );
		fillRect( g( 3 ), g( 11 ), g( 26 ), g( 10 ) );
		drawRect( g( 3 ), g( 11 ), g( 26 ), g( 10 ) );
		drawLine( g( 9 ), g( 13 ), g( 9 ), g( 19 ) );
	}

	public static void main( String[] commands ) {
		ProgramIcon.proof( new WorkareaRenameIcon() );
	}

}
