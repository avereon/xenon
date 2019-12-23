package com.avereon.xenon.event;

import com.avereon.xenon.ProductEventOld;
import com.avereon.xenon.workspace.Workarea;

public class WorkareaChangedEventOld extends ProductEventOld {

	private Workarea workarea;

	public WorkareaChangedEventOld( Object source, Workarea workarea ) {
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
