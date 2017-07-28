package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.Program;
import javafx.event.Event;

public class AboutAction extends Action {

	public AboutAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		program.getResourceManager().open( program.getResourceManager().createResource( "program:about" ) );
	}

}
