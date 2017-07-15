package com.parallelsymmetry.essence.resource.event;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceEvent;

import static com.parallelsymmetry.essence.resource.ResourceEvent.Type.REFRESHED;

public class ResourceRefreshedEvent extends ResourceEvent {

	public ResourceRefreshedEvent( Object source, Resource resource ) {
		super( source, REFRESHED, resource );
	}

}
