package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class ExitIcon extends ProgramIcon {

	@Override
	protected void render() {
		double radius = Math.sqrt( 2 * (J * J) );

		beginPath();
		moveTo( C, G );
		lineTo( O, K );
		arc( E, D, radius, radius, 135, -180 );
		lineTo( H, C );
		lineTo( P, O );
		arc( E, E, radius, radius, 45, -180 );
		lineTo( C, H );
		lineTo( L, P );
		arc( D, E, radius, radius, 315, -180 );
		lineTo( G, C );
		lineTo( K, L );
		arc( D, D, radius, radius, 225, -180 );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new ExitIcon() );
	}

}
