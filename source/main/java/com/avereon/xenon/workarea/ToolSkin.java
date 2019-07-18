package com.avereon.xenon.workarea;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;

public class ToolSkin extends SkinBase<Tool> {

	ToolSkin( Tool control ) {
		super( control );
	}

	@Override
	protected void layoutChildren( double contentX, double contentY, double contentWidth, double contentHeight ) {
		getChildren().forEach( c -> layoutInArea( c, contentX, contentY, contentWidth, contentHeight, -1, HPos.LEFT, VPos.TOP ) );
	}

}
