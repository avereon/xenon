package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;

public class ResourceClosedEvent extends ResourceEvent {

	public ResourceClosedEvent( Object source, Resource resource ) {
		super( source, resource );
	}

}
