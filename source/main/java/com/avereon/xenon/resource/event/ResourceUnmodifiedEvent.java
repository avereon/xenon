package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

public class ResourceUnmodifiedEvent extends ResourceEvent {

	public ResourceUnmodifiedEvent( Object source, Resource resource ) {
		super( source, Type.UNMODIFIED, resource );
	}

}
