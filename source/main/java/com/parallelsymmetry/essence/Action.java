package com.parallelsymmetry.essence;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public abstract class Action<T extends ActionEvent> implements EventHandler<T> {

	protected Program program;

	public Action( Program program ) {
		this.program = program;
	}

	public abstract boolean isEnabled();

}
