package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import javafx.event.ActionEvent;

public class CloseWorkspaceAction extends ProgramAction {

	public CloseWorkspaceAction( Xenon program ) {
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
