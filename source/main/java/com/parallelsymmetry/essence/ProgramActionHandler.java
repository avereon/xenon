package com.parallelsymmetry.essence;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public abstract class ProgramActionHandler<T extends ActionEvent> implements EventHandler<T> {

	protected Program program;

	public ProgramActionHandler( Program program ) {
		this.program = program;
	}

	public boolean isEnabled() {
		return false;
	}

}
