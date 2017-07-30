package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

public class ResourceSavedEvent extends ResourceEvent {

	public ResourceSavedEvent( Object source, Resource resource ) {
		super( source, Type.SAVED, resource );
	}

}
