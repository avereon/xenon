package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.update.ProgramProductManager;
import javafx.event.ActionEvent;

public class UpdateAction extends Action {

	public UpdateAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		((ProgramProductManager)getProgram().getProductManager()).checkForUpdates( true );
	}

}
