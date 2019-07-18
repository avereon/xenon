package com.avereon.xenon.workarea;

public class WorkpaneVetoException extends Exception {

	private static final long serialVersionUID = 5694681530452837687L;

	private WorkpaneEvent event;

	public WorkpaneVetoException( WorkpaneEvent event ) {
		this.event = event;
	}

	public WorkpaneVetoException( WorkpaneEvent event, String message ) {
		super( message );
		this.event = event;
	}

	public WorkpaneVetoException( WorkpaneEvent event, Throwable cause ) {
		super( cause );
		this.event = event;
	}

	public WorkpaneVetoException( WorkpaneEvent event, String message, Throwable cause ) {
		super( message, cause );
		this.event = event;
	}

	public WorkpaneEvent getEvent() {
		return event;
	}

}
