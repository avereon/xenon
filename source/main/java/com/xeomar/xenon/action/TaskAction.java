package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramTaskType;
import javafx.event.Event;

public class TaskAction extends Action {

	public TaskAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		getProgram().getResourceManager().open( ProgramTaskType.URI );
	}

}
