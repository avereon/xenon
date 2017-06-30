package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;

public class ResourceSavedEvent extends ResourceEvent {

	public ResourceSavedEvent( Object source, Resource resource ) {
		super( source, resource );
	}

}
