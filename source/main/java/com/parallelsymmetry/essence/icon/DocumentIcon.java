package com.parallelsymmetry.essence.icon;

import com.parallelsymmetry.essence.ProgramIcon;
import javafx.scene.paint.Color;

public class DocumentIcon extends ProgramIcon {

	@Override
	protected void render() {
		beginPath();
		moveTo( ZC, ZB );
		lineTo( ZC, ZO );
		lineTo( ZN, ZO );
		lineTo( ZN, ZF );
		lineTo( ZJ, ZB );
		closePath();
		fillAndDraw();

		beginPath();
		moveTo( ZN, ZF );
		lineTo( ZJ, ZB );
		lineTo( ZJ, ZF );
		closePath();
		fillAndDraw( new Color( 0.6, 0.6, 0.6, 1.0 ) );
	}

}
