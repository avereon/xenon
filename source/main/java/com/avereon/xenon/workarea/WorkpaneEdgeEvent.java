package com.avereon.xenon.workarea;

public class WorkpaneEdgeEvent extends WorkpaneEvent {

	private WorkpaneEdge edge;

	private double position;

	public WorkpaneEdgeEvent( Object source, Type type, Workpane workpane, WorkpaneEdge edge, double position ) {
		super( source, type, workpane );
		this.edge = edge;
		this.position = position;
	}

	public WorkpaneEdge getEdge() {
		return edge;
	}

	public double getPosition() {
		return position;
	}

}
