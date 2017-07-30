package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.ProgramEvent;
import com.xeomar.xenon.resource.Resource;

public class CurrentResourceChangedEvent extends ProgramEvent {

	private Resource current;

	private Resource previous;

	public CurrentResourceChangedEvent( Object source, Resource previous, Resource current ) {
		super( source );
		this.current = current;
		this.previous = previous;
	}

	public Resource getCurrentResource() {
		return current;
	}

	public Resource getPreviousResource() {
		return previous;
	}

}
