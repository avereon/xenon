package com.xeomar.xenon.resource.event;

import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceEvent;

public class ResourceRefreshedEvent extends ResourceEvent {

	public ResourceRefreshedEvent( Object source, Resource resource ) {
		super( source, Type.REFRESHED, resource );
	}

}
