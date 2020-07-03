package com.avereon.xenon;

import com.avereon.util.Log;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.lang.System.Logger;

public abstract class Action implements EventHandler<ActionEvent> {

	protected static final Logger log = Log.get();

	private static final ActionProxy NONE = new ActionProxy();

	private final Program program;

	private ActionProxy proxy = NONE;

	protected Action( Program program ) {
		if( program == null ) throw new NullPointerException( "Program cannot be null" );
		this.program = program;
	}

	public final Program getProgram() {
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

	@SuppressWarnings( "UnusedReturnValue" )
	public Action updateEnabled() {
		if( proxy != null ) proxy.setEnabled( isEnabled() );
		return this;
	}

	/**
	 * Get the current state of the action for multi-state actions.
	 *
	 * @return The state id
	 */
	public String getState() {
		return proxy.getState();
	}

	/**
	 * Set the action state for multi-state actions.
	 *
	 * @param id The state id
	 */
	public void setState( String id ) {
		Platform.runLater( () ->  proxy.setState( id ) );
	}

	void setActionProxy( ActionProxy proxy ) {
		this.proxy = proxy == null ? NONE : proxy;
	}

}
