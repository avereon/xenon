package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.ProgramEvent;
import com.parallelsymmetry.essence.resource.Resource;

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
