package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

import static com.avereon.xenon.resource.ResourceEvent.Type.CLOSED;

public class ResourceClosedEvent extends ResourceEvent {

	public ResourceClosedEvent( Object source, Resource resource ) {
		super( source, CLOSED, resource );
	}

}
