package com.xeomar.xenon.workarea;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * The ToolPane class provides a custom tab pane component for the program
 * allowing for extended and custom capabilities.
 */
public class ToolPane extends BorderPane {

	private ToolHeader header;

	//private CardPane;

	public ToolPane() {
		// Create components
		header = new ToolHeader();

		// Organize components
		setTop( header );
	}

	public ObservableList<ToolTab> getTabs() {
		return header.getTabs();
	}

	void setTool( Tool tool ) {
		getChildren().clear();
		if( tool != null ) setCenter( tool );
	}

	private class ToolHeader extends HBox {

		// Contains the ToolTab instances and keeps them in order
		private ObservableList<ToolTab> tabs;

		ToolHeader() {
			tabs = new SimpleListProperty<>();
		}

		ObservableList<ToolTab> getTabs() {
			return tabs;
		}

	}

}
