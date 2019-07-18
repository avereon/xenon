package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import javafx.event.ActionEvent;

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
	public void handle( ActionEvent event ) {
		getProgram().getResourceManager().open( ProgramSettingsType.URI );
	}

}
