package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

public class CurrentResourceChangedEvent extends ResourceEvent {

	private Resource previous;

	public CurrentResourceChangedEvent( Object source, Resource previous, Resource current ) {
		super( source, current );
		this.previous = previous;
	}

	public Resource getPreviousResource() {
		return previous;
	}

}
