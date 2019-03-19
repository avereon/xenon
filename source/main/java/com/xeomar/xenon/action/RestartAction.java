package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import javafx.event.ActionEvent;

public class RestartAction extends Action {

	public RestartAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		try {
			getProgram().requestRestart();
		} catch( Throwable throwable ) {
			log.error( "Error requesting restart", throwable );
		}
	}

}
