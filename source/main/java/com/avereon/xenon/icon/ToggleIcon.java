package com.avereon.xenon.icon;

import com.avereon.xenon.ProgramIcon;

public class ToggleIcon extends ProgramIcon {

	private boolean enabled;

	public ToggleIcon( Boolean enabled ) {
		this.enabled = enabled;
	}

	@Override
	protected void render() {
		double r = 7;
		double w = r + 1;
		double e = 32 - r - 1;

		startPath();
		moveTo( g( w ), g( 16 - r ) );
		addArc( g( w ), g( 16 ), g( r ), g( r ), 90, 180 );
		lineTo( g( e ), g( 16 + r ) );
		addArc( g( e ), g( 16 ), g( r ), g( r ), 270, 180 );
		closePath();

		fill( enabled ? GradientTone.MEDIUM : GradientTone.DARK );
		draw();

		if( enabled ) {
			setFillTone( GradientTone.LIGHT );
			fillCenteredOval( g( e ), g( 16 ), g( r ), g( r ) );
			drawCenteredOval( g( e ), g( 16 ), g( r ), g( r ) );
		} else {
			setFillTone( GradientTone.MEDIUM );
			fillCenteredOval( g( w ), g( 16 ), g( r ), g( r ) );
			drawCenteredOval( g( w ), g( 16 ), g( r ), g( r ) );
		}
	}

}
