package com.avereon.xenon.action;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import com.avereon.xenon.RestartHook;
import javafx.event.ActionEvent;

public class RestartAction extends ProgramAction {

	private static final System.Logger log = Log.get();

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
			getProgram().requestRestart( RestartHook.Mode.RESTART );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR, "Error requesting restart", throwable );
		}
	}

}
