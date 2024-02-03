package com.avereon.xenon.workspace;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import lombok.Getter;

@Getter
public class WorkareaMenuItem extends CustomMenuItem {

	private final Workarea workarea;

	public WorkareaMenuItem( Workarea workarea ) {
		this.workarea = workarea;

		getStyleClass().addAll( "menu-item", "workarea-menu-item" );

		Label label = new Label();
		label.getStyleClass().addAll("label");
		label.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i ) ) );
		label.textProperty().bind( workarea.nameProperty() );
		setContent( label );

		//getStyleableNode().setOnMousePressed( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
	}

}
