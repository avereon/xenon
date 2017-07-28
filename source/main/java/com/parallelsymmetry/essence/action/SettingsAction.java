package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.type.ProgramSettingsType;
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
