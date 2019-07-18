package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import javafx.event.ActionEvent;

public class CloseWorkspaceAction extends Action {

	public CloseWorkspaceAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getWorkspaceManager().requestCloseWorkspace( getProgram().getWorkspaceManager().getActiveWorkspace() );
	}

}
