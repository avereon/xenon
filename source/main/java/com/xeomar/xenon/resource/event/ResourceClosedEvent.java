package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

import static com.xeomar.xenon.resource.ResourceEvent.Type.CLOSED;

public class ResourceClosedEvent extends ResourceEvent {

	public ResourceClosedEvent( Object source, Resource resource ) {
		super( source, CLOSED, resource );
	}

}
