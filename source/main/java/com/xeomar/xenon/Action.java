package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public abstract class Action<T extends ActionEvent> implements EventHandler<T> {

	protected static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private boolean enabled;

	protected Action( Program program ) {
		this.program = program;
	}

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
