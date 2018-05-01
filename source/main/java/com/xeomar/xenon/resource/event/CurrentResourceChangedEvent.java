package com.xeomar.xenon.resource.event;

import com.xeomar.product.ProductEvent;
import com.xeomar.xenon.resource.Resource;

public class CurrentResourceChangedEvent extends ProductEvent {

	private Resource current;

	private Resource previous;

	public CurrentResourceChangedEvent( Object source, Resource previous, Resource current ) {
		super( source );
		this.current = current;
		this.previous = previous;
	}

	public Resource getCurrentResource() {
		return current;
	}

	public Resource getPreviousResource() {
		return previous;
	}

}
