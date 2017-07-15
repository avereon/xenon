package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

import static com.parallelsymmetry.essence.resource.ResourceEvent.Type.CLOSED;

public class ResourceClosedEvent extends ResourceEvent {

	public ResourceClosedEvent( Object source, Resource resource ) {
		super( source, CLOSED, resource );
	}

}
