package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
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
		program.getResourceManager().open( program.getResourceManager().createResource( ProgramSettingsType.URI ) );
	}

}
