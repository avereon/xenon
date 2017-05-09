package com.parallelsymmetry.essence.icon;

public class CloseIcon extends DocumentIcon {

	public void render() {
		super.render();

		beginPath();
		moveTo( ZF, ZH );
		lineTo( ZK, ZM );
		moveTo( ZK, ZH );
		lineTo( ZF, ZM );

		setLineWidth( g( 1, 16 ) );
		draw();
	}

}
