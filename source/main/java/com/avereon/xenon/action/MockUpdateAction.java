package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.RestartJob;
import javafx.event.ActionEvent;

public class MockUpdateAction extends ProgramAction {

	public MockUpdateAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().requestRestart( RestartJob.Mode.MOCK_UPDATE );
	}

}
