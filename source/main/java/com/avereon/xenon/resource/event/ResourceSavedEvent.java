package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

public class ResourceSavedEvent extends ResourceEvent {

	public ResourceSavedEvent( Object source, Resource resource ) {
		super( source, Type.SAVED, resource );
	}

}
