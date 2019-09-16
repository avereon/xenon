package com.avereon.xenon.resource;

import com.avereon.product.ProductEvent;

public abstract class ResourceEvent extends ProductEvent {

	public enum Type {
		OPENED,
		LOADED,
		READY,
		REFRESHED,
		MODIFIED,
		UNMODIFIED,
		SAVED,
		CLOSED
	}

	private Type type;

	private Resource resource;

	protected ResourceEvent( Object source, Type type, Resource resource ) {
		super( source );
		this.type = type;
		this.resource = resource;
	}

	public Type getType() {
		return type;
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
