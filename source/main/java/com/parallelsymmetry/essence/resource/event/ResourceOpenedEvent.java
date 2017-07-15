package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

public class ResourceOpenedEvent extends ResourceEvent {

	public ResourceOpenedEvent( Object source, Resource resource ) {
		super( source, resource );
	}

}
