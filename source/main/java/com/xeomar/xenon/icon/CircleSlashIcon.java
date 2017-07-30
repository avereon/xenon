package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import javafx.scene.paint.Color;

public class CircleSlashIcon extends ProgramIcon {

	protected void render() {
		setFillPaint( Color.BLUE );
		fillOval( g( 2 ), g( 2 ), g( 30 ), g( 30 ) );

		setLineWidth( g( 6 ) );
		setDrawPaint( Color.CORNFLOWERBLUE );
		drawLine( g( 4 ), g( 28 ), g( 28 ), g( 4 ) );
	}

	public static void main( String[] commands ) {
		proof( new CircleSlashIcon() );
	}

}
