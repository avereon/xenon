package com.xeomar.xenon;

public enum ExecMode {

	DEVL( "@"),
	TEST( "#");

	private String prefix;

	ExecMode( String prefix ) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

}
