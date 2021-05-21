package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionLibrary;
import com.avereon.xenon.Program;
import javafx.scene.control.ToolBar;

public class ToolBarFactory extends BarFactory {

	public ToolBar createToolBar( Program program, String descriptor ) {
		return createToolBar( program.getActionLibrary(), descriptor);
	}

	ToolBar createToolBar( ActionLibrary actions, String descriptor ) {
		return null;
	}

}
