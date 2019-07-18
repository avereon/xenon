package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramAboutType;
import javafx.event.ActionEvent;

public class AboutAction extends Action {

	public AboutAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getResourceManager().open( ProgramAboutType.URI );
	}

}
