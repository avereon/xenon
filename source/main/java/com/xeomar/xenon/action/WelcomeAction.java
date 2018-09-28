package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramWelcomeType;
import javafx.event.Event;

public class WelcomeAction extends Action {

	public WelcomeAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		try {
			getProgram().getResourceManager().open( ProgramWelcomeType.URI );
		} catch( ResourceException exception ) {
			log.error( "Error opening welcome tool", exception );
		}
	}

}
