package com.avereon.xenon;

import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneView;

public class OpenToolRequest {

	private OpenAssetRequest openAssetRequest;

	private Asset asset;

	private Workpane pane;

	private String id;

	private Class<? extends ProgramTool> toolClass;

	public OpenToolRequest( OpenAssetRequest openAssetRequest ) {
		this.openAssetRequest = openAssetRequest;
	}

	public String getQuery() {return openAssetRequest.getQuery();}

	public String getFragment() {return openAssetRequest.getFragment();}

	public Codec getCodec() {return openAssetRequest.getCodec();}

	public WorkpaneView getView() {return openAssetRequest.getView();}

	public boolean isOpenTool() {return openAssetRequest.isOpenTool();}

	public boolean isSetActive() {return openAssetRequest.isSetActive();}

	public String getId() {
		return id;
	}

	public OpenToolRequest setId( String id ) {
		this.id = id;
		return this;
	}

	public Class<? extends ProgramTool> getToolClass() {
		return toolClass;
	}

	public OpenToolRequest setToolClass( Class<? extends ProgramTool> toolClass ) {
		this.toolClass = toolClass;
		return this;
	}

	public Asset getAsset() {
		return asset;
	}

	public OpenToolRequest setAsset( Asset asset ) {
		this.asset = asset;
		return this;
	}

	public Workpane getPane() {
		return pane;
	}

	public OpenToolRequest setPane( Workpane pane ) {
		this.pane = pane;
		return this;
	}

}
