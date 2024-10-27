package com.avereon.xenon.workpane;

import lombok.Getter;

@Getter
public class ToolInstantiationException extends ToolException {

	private final String id;

	private final String toolClass;

	public ToolInstantiationException( String id, String toolClass ) {
		this( id, toolClass, null, null );
	}

	public ToolInstantiationException( String id, String toolClass, String message ) {
		this( id, toolClass, message, null );
	}

	public ToolInstantiationException( String id, String toolClass, Throwable cause ) {
		this( id, toolClass, null, cause );
	}

	public ToolInstantiationException( String id, String toolClass, String message, Throwable cause ) {
		super( message, cause );
		this.id = id;
		this.toolClass = toolClass;
	}

}
