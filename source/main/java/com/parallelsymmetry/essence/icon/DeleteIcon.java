package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class DeleteIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		arc( g2( 1 ), g4( 3 ), g32( 9 ), g32( 3 ), 180, 180 );
		lineTo( g32( 25 ), g4( 1 ) );
		lineTo( g32( 7 ), g4( 1 ) );
		closePath();
		fillAndDraw();

		beginPath();
		arc( g2( 1 ), g4( 1 ), g32( 9 ), g32( 3 ), 0, 360 );
		fillAndDraw( GradientShade.DARK );
	}

	public static void main( String[] commands ) {
		proof( new DeleteIcon() );
	}

}
