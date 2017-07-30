package com.xeomar.xenon.workarea;

import com.xeomar.xenon.worktool.Tool;

import java.util.EventObject;

public class WorkpaneEvent  extends EventObject {

	public enum Type {
		CHANGED,
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

	private Tool tool;

	private WorkpaneView view;

	public WorkpaneEvent( Object source, Type type, Workpane workpane ) {
		this( source, type, workpane, null, null );
	}

	public WorkpaneEvent( Object source, Type type, Workpane workpane, WorkpaneView view, Tool tool ) {
		super( source );
		this.type = type;
		this.pane = workpane;
		this.view = view;
		this.tool = tool;
	}

	public Type getType() {
		return type;
	}

	public Workpane getWorkPane() {
		return pane;
	}

	@Deprecated
	public WorkpaneView getToolView() {
		return view;
	}

	public Tool getTool() {
		return tool;
	}

	@Override
	public String toString() {
		return type.name();
	}

}
