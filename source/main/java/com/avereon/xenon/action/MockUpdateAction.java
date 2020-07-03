package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.RestartHook;
import javafx.event.ActionEvent;

public class MockUpdateAction extends Action {

	public MockUpdateAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().requestRestart( RestartHook.Mode.MOCK_UPDATE );
	}

}
