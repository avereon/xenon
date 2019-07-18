package com.avereon.xenon.workarea;

import java.util.EventObject;

public class WorkpaneEvent  extends EventObject {

	public enum Type {
		CHANGED,
		EDGE_ADDED,
		EDGE_REMOVED,
		EDGE_MOVED,
		VIEW_ADDED,
		VIEW_REMOVED,
		VIEW_SPLIT,
		VIEW_WILL_SPLIT,
		VIEW_WILL_MERGE,
		VIEW_MERGED,
		VIEW_ACTIVATED,
		VIEW_DEACTIVATED,
		TOOL_ADDED,
		TOOL_REMOVED,
		TOOL_DISPLAYED,
		TOOL_CONCEALED,
		TOOL_ACTIVATED,
		TOOL_DEACTIVATED
	}

	private static final long serialVersionUID = 1884480622313348903L;

	private Type type;

	private Workpane pane;

	public WorkpaneEvent( Object source, Type type, Workpane workpane ) {
		super(source);
		this.type = type;
		this.pane = workpane;
	}

	public Type getType() {
		return type;
	}

	public Workpane getWorkPane() {
		return pane;
	}

	@Override
	public String toString() {
		return type.name();
	}

}
