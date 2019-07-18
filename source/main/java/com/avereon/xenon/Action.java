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

	protected Action( Program program ) {
		this.program = program;
	}

	public abstract void handle( ActionEvent event );

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public Program getProgram() {
		return program;
	}

}
