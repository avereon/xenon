package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

public class ResourceReadyEvent extends ResourceEvent {

	public ResourceReadyEvent( Object source, Resource resource ) {
		super( source, ResourceEvent.Type.READY, resource );
	}

}
