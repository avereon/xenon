package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import javafx.event.ActionEvent;

public class UpdateAction extends ProgramAction {

	public UpdateAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getProductManager().checkForUpdates( true );
	}

}
