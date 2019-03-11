package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import javafx.event.Event;

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
	public void handle( Event event ) {
		try {
			runnable.run();
		} catch( Throwable throwable ) {
			log.error( "Error running action", throwable );
		}
	}

}
