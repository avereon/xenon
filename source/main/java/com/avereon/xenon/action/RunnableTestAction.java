package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import javafx.event.ActionEvent;

public class RunnableTestAction extends Action {

	private Runnable runnable;

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
			log.error( "Error running action", throwable );
		}
	}

}
