package com.avereon.xenon.workspace;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

public class WorkareaMenuItem extends CustomMenuItem {

	public WorkareaMenuItem( Workarea workarea ) {
		Label label = new Label();

		label.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i ) ) );
		label.textProperty().bind( workarea.nameProperty() );

		setContent( label );
	}

}
