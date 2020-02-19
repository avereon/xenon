package com.avereon.xenon;

import com.avereon.util.Log;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.lang.System.Logger;

public abstract class Action implements EventHandler<ActionEvent> {

	protected static final Logger log = Log.get();

	private Program program;

	private ActionProxy proxy;

	protected Action( Program program ) {
		this.program = program;
	}

	public Program getProgram() {
		return program;
	}

	public abstract void handle( ActionEvent event );

	/**
	 * Override this method with the logic to determine if the action is enabled.
	 *
	 * @return If the action is enabled
	 */
	public boolean isEnabled() {
		return false;
	}

	public Action updateEnabled() {
		if( proxy != null ) proxy.setEnabled( isEnabled() );
		return this;
	}

	void setActionProxy( ActionProxy proxy ) {
		this.proxy = proxy;
	}

}
