package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

public class ResourceUnmodifiedEvent extends ResourceEvent {

	public ResourceUnmodifiedEvent( Object source, Resource resource ) {
		super( source, Type.UNMODIFIED, resource );
	}

}
