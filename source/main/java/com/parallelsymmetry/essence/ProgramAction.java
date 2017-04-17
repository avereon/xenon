package com.parallelsymmetry.essence;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public abstract class ProgramAction<T extends ActionEvent> implements EventHandler<T> {

	protected Program program;

	public ProgramAction( Program program ) {
		this.program = program;
	}

}
