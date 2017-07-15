package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

import static com.parallelsymmetry.essence.resource.ResourceEvent.Type.MODIFIED;

public class ResourceModifiedEvent extends ResourceEvent {

	public ResourceModifiedEvent( Object source, Resource resource ) {
		super( source, MODIFIED, resource );
	}

}
