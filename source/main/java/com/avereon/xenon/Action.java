package com.avereon.xenon;

import com.avereon.util.LogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public abstract class Action implements EventHandler<ActionEvent> {

	protected static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private boolean enabled;

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
		return enabled;
	}

	public Action updateEnabled() {
		if( proxy != null ) proxy.setEnabled( isEnabled() );
		return this;
	}

	@Deprecated
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	void setActionProxy( ActionProxy proxy ) {
		this.proxy = proxy;
	}

}
