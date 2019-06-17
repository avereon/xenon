package com.xeomar.xenon.workarea;

import javafx.scene.control.Control;

import java.util.List;

/**
 * The ToolPane class provides a custom tab pane component for the program
 * allowing for extended and custom capabilities.
 */
public class ToolPane extends Control {

	public ToolPane() {
		this( List.of() );
	}

	public ToolPane( List<ToolTab> tabs ) {}

}
