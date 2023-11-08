package com.avereon.xenon.workspace;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

public class WorkareaMenuItem extends CustomMenuItem {

	public WorkareaMenuItem( Workarea workarea ) {
		getStyleClass().addAll( "menu-item", "workarea-menu-item" );

		Label label = new Label();
		label.getStyleClass().addAll("label");
		label.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i ) ) );
		label.textProperty().bind( workarea.nameProperty() );
		setContent( label );

		setOnAction( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
	}

}
