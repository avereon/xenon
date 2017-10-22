package com.xeomar.xenon;

public enum ExecMode {

	DEV( "#" ),
	TEST( "$" ),
	PROD( "" );

	private String prefix;

	ExecMode( String prefix ) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

}
