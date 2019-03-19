package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramWelcomeType;
import javafx.event.ActionEvent;

public class WelcomeAction extends Action {

	public WelcomeAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getResourceManager().open( ProgramWelcomeType.URI );
	}

}
