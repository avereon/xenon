package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

public class ResourceClosedEvent extends ResourceEvent {

	public ResourceClosedEvent( Object source, Resource resource ) {
		super( source, resource );
	}

}
