package com.avereon.xenon.asset;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.workpane.WorkpaneView;

import java.net.URI;

public class OpenAssetRequest {

	private URI uri;

	private AssetType type;

	private Object model;

	private Codec codec;

	private WorkpaneView view;

	private boolean openTool;

	private boolean setActive;

	private Asset asset;

	private String id;

	private Class<? extends ProgramTool> toolClass;

	URI getUri() {
		return uri;
	}

	public OpenAssetRequest setUri( URI uri ) {
		this.uri = uri;
		return this;
	}

	public AssetType getType() {
		return type;
	}

	public OpenAssetRequest setType( AssetType type ) {
		this.type = type;
		return this;
	}

	@SuppressWarnings( "unchecked" )
	public <M> M getModel() {
		return (M)model;
	}

	public <M> OpenAssetRequest setModel( M model ) {
		this.model = model;
		return this;
	}

	public String getQuery() {
		return uri == null ? null : uri.getQuery();
	}

	public String getFragment() {
		return uri == null ? null : uri.getFragment();
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

	public Asset getAsset() {
		return asset;
	}

	public OpenAssetRequest setAsset( Asset asset ) {
		this.asset = asset;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public Class<? extends ProgramTool> getToolClass() {
		return toolClass;
	}

	public void setToolClass( Class<? extends ProgramTool> toolClass ) {
		this.toolClass = toolClass;
	}
}
