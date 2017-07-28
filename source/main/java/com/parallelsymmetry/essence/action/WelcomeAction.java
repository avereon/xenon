package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.resource.type.ProgramWelcomeType;
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
		program.getResourceManager().open( program.getResourceManager().createResource( ProgramWelcomeType.URI ) );
	}

}
