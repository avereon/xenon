package com.xeomar.xenon;

import com.xeomar.util.LogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;

// FIXME There is a proper Action class in JavaFX
public abstract class Action<T extends ActionEvent> implements EventHandler<T> {

	protected static Logger log = LogUtil.get( Action.class );

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
