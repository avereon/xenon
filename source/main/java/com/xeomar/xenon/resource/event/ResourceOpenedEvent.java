package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

import static com.xeomar.xenon.resource.ResourceEvent.Type.OPENED;

public class ResourceOpenedEvent extends ResourceEvent {

	public ResourceOpenedEvent( Object source, Resource resource ) {
		super( source, OPENED, resource );
	}

}
