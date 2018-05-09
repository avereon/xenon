package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramFlag;
import javafx.event.Event;

public class RestartAction extends Action {

	public RestartAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		getProgram().requestRestart( ProgramFlag.NOUPDATECHECK );
	}

}
