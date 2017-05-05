package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.paint.Color;

public class CircleSlashIcon extends ProgramIcon {

	protected void render() {
		setFillPaint( Color.BLUE );
		fillOval( 0, 0, 1, 1 );

		setLineWidth( g16( 3 ) );
		setDrawPaint( Color.CORNFLOWERBLUE );
		drawLine( g8( 1 ), g8( 7 ), g8( 7 ), g8( 1 ) );
	}

}
