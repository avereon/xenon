package com.xeomar.xenon.icon;

import com.xeomar.xenon.ProgramIcon;
import javafx.scene.shape.ArcType;

public class PowerIcon extends ProgramIcon {

	@Override
	protected void render() {
		double angle = 35;
		double radius = g( 12 );

		setLineWidth( g(3) );
		drawCenteredArc( g(16), g(18), radius, radius, 90 + angle, 360 - (2*angle), ArcType.OPEN );
		drawLine( g(16), g(4), g(16), g(14) );
	}

	public static void main( String[] commands ) {
		proof( new PowerIcon() );
	}

}
