package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

public class ResourceModifiedEvent extends ResourceEvent {

	public ResourceModifiedEvent( Object source, Resource resource ) {
		super( source, Type.MODIFIED, resource );
	}

}
