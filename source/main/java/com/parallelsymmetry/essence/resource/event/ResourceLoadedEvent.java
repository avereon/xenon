package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;

public class ResourceLoadedEvent extends ResourceEvent {

	public ResourceLoadedEvent( Object source, Resource resource ) {
		super( source, resource );
	}

}
