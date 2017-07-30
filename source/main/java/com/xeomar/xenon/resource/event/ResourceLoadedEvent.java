package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

public class ResourceLoadedEvent extends ResourceEvent {

	public ResourceLoadedEvent( Object source, Resource resource ) {
		super( source, Type.LOADED, resource );
	}

}
