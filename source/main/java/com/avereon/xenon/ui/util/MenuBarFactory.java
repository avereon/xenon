package com.avereon.xenon.ui.util;

import com.avereon.xenon.ActionLibrary;
import com.avereon.xenon.Program;
import javafx.scene.control.MenuBar;

public class MenuBarFactory extends BarFactory {

	public MenuBar createMenuBar( Program program, String descriptor ) {
		return createMenuBar( program.getActionLibrary(), descriptor);
	}

	MenuBar createMenuBar( ActionLibrary actions, String descriptor ) {
		return null;
	}

}
