package com.xeomar.xenon.workarea;

public class WorkpaneViewEvent extends WorkpaneEvent {

	private WorkpaneView view;

	public WorkpaneViewEvent( Object source, Type type, Workpane workpane, WorkpaneView view ) {
		super( source, type, workpane );
		this.view = view;
	}

	public WorkpaneView getView() {
		return view;
	}

}
