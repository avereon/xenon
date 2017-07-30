package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

public class ResourceModifiedEvent extends ResourceEvent {

	public ResourceModifiedEvent( Object source, Resource resource ) {
		super( source, Type.MODIFIED, resource );
	}

}
