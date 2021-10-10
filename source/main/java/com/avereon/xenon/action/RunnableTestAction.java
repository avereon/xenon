package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import javafx.event.ActionEvent;
import lombok.CustomLog;

@CustomLog
public class RunnableTestAction extends ProgramAction {

	private final Runnable runnable;

	public RunnableTestAction( Program program, Runnable runnable ) {
		super( program );
		this.runnable = runnable;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		try {
			runnable.run();
		} catch( Throwable throwable ) {
			log.atError(throwable).log( "Error running action" );
		}
	}

}
