package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.RestartHook;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class RestartAction extends ProgramAction {

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
			log.atError( throwable ).log( "Error requesting restart" );
		}
	}

}
