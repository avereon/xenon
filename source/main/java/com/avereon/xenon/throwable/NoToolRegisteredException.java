package com.avereon.xenon.throwable;

public class NoToolRegisteredException extends Exception {

	private String toolClassName;

	public NoToolRegisteredException( String toolClassName ) {
		this.toolClassName = toolClassName;
	}

	public String getToolClassName() {
		return toolClassName;
	}

}
