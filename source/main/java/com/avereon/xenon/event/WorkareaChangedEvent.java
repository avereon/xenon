package com.avereon.xenon.event;

import com.avereon.product.ProductEvent;
import com.avereon.xenon.workarea.Workarea;

public class WorkareaChangedEvent extends ProductEvent {

	private Workarea workarea;

	public WorkareaChangedEvent( Object source, Workarea workarea ) {
		super( source );
		this.workarea = workarea;
	}

	public Workarea getWorkarea() {
		return workarea;
	}

	public String toString() {
		return super.toString() + ":" + ( workarea == null ? "null" : workarea.getName());
	}

}
