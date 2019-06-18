package com.xeomar.xenon.workarea;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.Labeled;

public class ToolTab extends Labeled {

	private Tool tool;

	private BooleanProperty selected;

	public ToolTab( Tool tool) {
		this.tool = tool;
		this.selected = new SimpleBooleanProperty( this, "selected" );
	}

	public ObservableBooleanValue selectedProperty() {
		return selected;
	}

	public Boolean isSelected() {
		return selected.get();
	}

	public void setSelected( Boolean selected ) {
		this.selected.set( selected );
	}

	public Tool getContent() {
		return getTool();
	}

	public Tool getTool() {
		return tool;
	}

}
