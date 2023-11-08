package com.avereon.xenon.workspace;

import javafx.geometry.Insets;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

public class WorkareaMenuItem extends CustomMenuItem {

	public WorkareaMenuItem( Workarea workarea ) {
		Label label = new Label();

		label.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i ) ) );
		label.textProperty().bind( workarea.nameProperty() );
		label.backgroundProperty().bind( workarea.paintProperty().map(p -> new Background( new BackgroundFill(p, CornerRadii.EMPTY, Insets.EMPTY) ) ));

		setContent( label );

		setOnAction( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
	}

}
