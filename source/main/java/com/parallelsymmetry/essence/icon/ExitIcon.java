package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;

public class ExitIcon extends ProgramIcon {

	@Override
	protected void render() {
		double radius = Math.sqrt( 2 * (J * J) );

		// FIXME Use arc() instead of arcTo()
		beginPath();
		moveTo( C, G );
		lineTo( O, K );
		arcTo( E, F, P, K, radius );
		arcTo( I, D, P, L, radius );
		lineTo( H, C );
		lineTo( P, O );
		arcTo( I, E, P, P, radius );
		arcTo( E, I, O, P, radius );
		lineTo( C, H );
		lineTo( L, P );
		arcTo( D, I, K, P, radius );
		arcTo( F, E, K, O, radius );
		lineTo( G, C );
		lineTo( K, L );
		arcTo( F, D, K, K, radius );
		arcTo( D, F, L, K, radius );
		closePath();

		fillAndDraw();
	}

	public static void main( String[] commands ) {
		proof( new ExitIcon() );
	}

}
