package com.parallelsymmetry.essence.event;

import com.parallelsymmetry.essence.ProgramEvent;
import com.parallelsymmetry.essence.workarea.Workarea;

public class WorkareaChangedEvent extends ProgramEvent {

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
