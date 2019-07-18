package com.avereon.xenon.workarea;

import com.avereon.xenon.OpenToolRequest;

public class ToolParameters {

	private String query;

	private String fragment;

	public String getQuery() {
		return query;
	}

	public ToolParameters( OpenToolRequest request ) {
		setQuery( request.getQuery() );
		setFragment( request.getFragment() );
	}

	public ToolParameters setQuery( String query ) {
		this.query = query;
		return this;
	}

	public String getFragment() {
		return fragment;
	}

	public ToolParameters setFragment( String fragment ) {
		this.fragment = fragment;
		return this;
	}

}
