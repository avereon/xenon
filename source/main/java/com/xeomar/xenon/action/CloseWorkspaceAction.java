package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
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
