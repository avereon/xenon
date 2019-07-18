package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

public class ResourceLoadedEvent extends ResourceEvent {

	public ResourceLoadedEvent( Object source, Resource resource ) {
		super( source, Type.LOADED, resource );
	}

}
