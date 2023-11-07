package com.avereon.xenon.workspace;

import javafx.scene.control.ListCell;

public class WorkareaSelectorCell extends ListCell<WorkareaSelectorItem> {

	@Override
	protected void updateItem( WorkareaSelectorItem item, boolean empty ) {
		super.updateItem( item, empty );
		if( item == null || empty ) {
			//backgroundProperty().unbind();
			graphicProperty().unbind();
			textProperty().unbind();
		} else {
			// Setting the style here overrides the normal behavior
			//setStyle( "-fx-background-color: green;" );
			//backgroundProperty().bind( item.paintProperty().map( p -> new Background( new BackgroundFill( p, CornerRadii.EMPTY, Insets.EMPTY ) ) ) );
			graphicProperty().bind( item.graphicProperty() );
			textProperty().bind( item.textProperty() );
		}
	}

}
