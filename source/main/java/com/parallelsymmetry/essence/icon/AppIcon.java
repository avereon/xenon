package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;

public class AppIcon extends ProgramIcon {

	@Override
	protected void configure( Group group ) {
		Line line = new Line( g8( 1 ), g8( 7 ), g8( 7 ), g8( 1 ) );
		line.setStrokeWidth( g16( 3 ) );
		line.setStroke( Color.CORNFLOWERBLUE );

		Ellipse circle = new Ellipse( 0.5, 0.5, 0.5, 0.5 );
		circle.setFill( Color.BLUE.darker() );

		add( circle, line );
	}

}
