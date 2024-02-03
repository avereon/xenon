package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import javafx.event.ActionEvent;

public class UpdateAction extends ProgramAction {

	public UpdateAction( Xenon program ) {
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
