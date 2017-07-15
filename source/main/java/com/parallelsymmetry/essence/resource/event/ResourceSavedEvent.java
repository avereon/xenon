package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

import static com.parallelsymmetry.essence.resource.ResourceEvent.Type.SAVED;

public class ResourceSavedEvent extends ResourceEvent {

	public ResourceSavedEvent( Object source, Resource resource ) {
		super( source, SAVED, resource );
	}

}
