package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.ProgramEvent;
import com.parallelsymmetry.essence.resource.Resource;

public class ResourceEvent extends ProgramEvent {

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
