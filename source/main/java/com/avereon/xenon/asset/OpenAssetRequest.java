package com.avereon.xenon.asset;

import com.avereon.xenon.workpane.WorkpaneView;

import java.net.URI;

public class OpenAssetRequest {

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

	public OpenAssetRequest setUri( URI uri ) {
		this.uri = uri;
		return this;
	}

	public String getQuery() {
		return query;
	}

	public OpenAssetRequest setQuery( String query ) {
		this.query = query;
		return this;
	}

	public String getFragment() {
		return fragment;
	}

	public OpenAssetRequest setFragment( String fragment ) {
		this.fragment = fragment;
		return this;
	}

	public Codec getCodec() {
		return codec;
	}

	public OpenAssetRequest setCodec( Codec codec ) {
		this.codec = codec;
		return this;
	}

	public WorkpaneView getView() {
		return view;
	}

	public OpenAssetRequest setView( WorkpaneView view ) {
		this.view = view;
		return this;
	}

	public boolean isOpenTool() {
		return openTool;
	}

	public OpenAssetRequest setOpenTool( boolean openTool ) {
		this.openTool = openTool;
		return this;
	}

	public boolean isSetActive() {
		return setActive;
	}

	public OpenAssetRequest setSetActive( boolean setActive ) {
		this.setActive = setActive;
		return this;
	}

}