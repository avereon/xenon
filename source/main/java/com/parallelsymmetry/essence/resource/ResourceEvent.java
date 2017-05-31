package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.Resource;

import java.util.EventObject;

public class ResourceEvent extends EventObject {

	private Resource resource;

	public ResourceEvent( Object source, Resource resource ) {
		super( source );
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		Resource resource = getResource();
		if( resource == null ) return super.toString() + ": null";
		return super.toString() + ": " + resource.toString();
	}

}
