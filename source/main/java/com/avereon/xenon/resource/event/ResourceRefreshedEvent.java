package com.avereon.xenon.resource.event;

import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;

public class ResourceRefreshedEvent extends ResourceEvent {

	public ResourceRefreshedEvent( Object source, Resource resource ) {
		super( source, Type.REFRESHED, resource );
	}

}
