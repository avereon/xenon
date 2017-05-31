package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.Action;
import javafx.event.Event;

public class Exit extends Action {

	public Exit( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		program.requestExit();
	}

}
