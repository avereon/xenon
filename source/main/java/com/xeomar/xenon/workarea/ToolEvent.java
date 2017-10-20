package com.xeomar.xenon.workarea;

import com.xeomar.xenon.workarea.Tool;

import java.util.EventObject;

public class ToolEvent extends EventObject {

	public enum Type {
		TOOL_CLOSING, TOOL_CLOSED
	}

	private static final long serialVersionUID = -4975302925133248236L;

	private Type type;

	private Tool tool;

	public ToolEvent( Object source, Type type, Tool tool ) {
		super( source );
		this.type = type;
		this.tool = tool;
	}

	public Type getType() {
		return type;
	}

	public Tool getTool() {
		return tool;
	}

}
