package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

public class ResourceSavedEvent extends ResourceEvent {

	public ResourceSavedEvent( Object source, Resource resource ) {
		super( source, resource );
	}

}
