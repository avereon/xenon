package com.avereon.xenon.resource;

import com.avereon.xenon.workarea.WorkpaneView;

import java.net.URI;

public class OpenResourceRequest {

	private URI uri;

	private String query;

	private String fragment;

	private Codec codec;

	private WorkpaneView view;

	private boolean openTool;

	private boolean setActive;

	URI getUri() {
		return uri;
	}

	public OpenResourceRequest setUri( URI uri ) {
		this.uri = uri;
		return this;
	}

	public String getQuery() {
		return query;
	}

	public OpenResourceRequest setQuery( String query ) {
		this.query = query;
		return this;
	}

	public String getFragment() {
		return fragment;
	}

	public OpenResourceRequest setFragment( String fragment ) {
		this.fragment = fragment;
		return this;
	}

	public Codec getCodec() {
		return codec;
	}

	public OpenResourceRequest setCodec( Codec codec ) {
		this.codec = codec;
		return this;
	}

	public WorkpaneView getView() {
		return view;
	}

	public OpenResourceRequest setView( WorkpaneView view ) {
		this.view = view;
		return this;
	}

	public boolean isOpenTool() {
		return openTool;
	}

	public OpenResourceRequest setOpenTool( boolean openTool ) {
		this.openTool = openTool;
		return this;
	}

	public boolean isSetActive() {
		return setActive;
	}

	public OpenResourceRequest setSetActive( boolean setActive ) {
		this.setActive = setActive;
		return this;
	}

}
