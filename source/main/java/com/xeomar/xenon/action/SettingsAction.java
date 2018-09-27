package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.type.ProgramSettingsType;
import javafx.event.Event;

public class SettingsAction extends Action {

	private Resource resource;

	public SettingsAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		try {
			getProgram().getResourceManager().open( ProgramSettingsType.URI );
		} catch( ResourceException exception ) {
			log.error( "Error opening settings tool", exception );
		}
	}

}
