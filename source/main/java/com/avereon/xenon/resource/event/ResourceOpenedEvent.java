package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

import static com.avereon.xenon.resource.ResourceEvent.Type.OPENED;

public class ResourceOpenedEvent extends ResourceEvent {

	public ResourceOpenedEvent( Object source, Resource resource ) {
		super( source, OPENED, resource );
	}

}
