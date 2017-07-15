package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

import static com.parallelsymmetry.essence.resource.ResourceEvent.Type.LOADED;

public class ResourceLoadedEvent extends ResourceEvent {

	public ResourceLoadedEvent( Object source, Resource resource ) {
		super( source, LOADED, resource );
	}

}
