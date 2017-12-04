package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramAboutType;
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
		try {
			getProgram().getResourceManager().open( ProgramAboutType.uri );
		} catch( ResourceException exception ) {
			log.error( "Error opening about tool", exception );
		}
	}

}
