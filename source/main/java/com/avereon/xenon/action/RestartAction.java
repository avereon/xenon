package com.avereon.xenon.action;

import com.avereon.util.Log;
import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramShutdownHook;
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
			getProgram().requestRestart( ProgramShutdownHook.Mode.RESTART );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR, "Error requesting restart", throwable );
		}
	}

}
