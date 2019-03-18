package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.update.ProgramProductManager;
import javafx.event.Event;

public class TestUpdateDialogAction extends Action {

	public TestUpdateDialogAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		try {
			((ProgramProductManager)getProgram().getProductManager()).showUpdateFoundDialog();
		} catch( Throwable throwable ) {
			log.error( "Error showing update found dialog", throwable );
		}
	}

}
