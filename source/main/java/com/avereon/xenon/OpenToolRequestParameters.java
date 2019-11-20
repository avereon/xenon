package com.avereon.xenon;

public class OpenToolRequestParameters {

	private String query;

	private String fragment;

	public String getQuery() {
		return query;
	}

	public OpenToolRequestParameters( OpenToolRequest request ) {
		setQuery( request.getQuery() );
		setFragment( request.getFragment() );
	}

	public OpenToolRequestParameters setQuery( String query ) {
		this.query = query;
		return this;
	}

	public String getFragment() {
		return fragment;
	}

	public OpenToolRequestParameters setFragment( String fragment ) {
		this.fragment = fragment;
		return this;
	}

}
