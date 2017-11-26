package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import javafx.event.Event;

public class ExitAction extends Action {

	public ExitAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		getProgram().requestExit( false );
	}

}
