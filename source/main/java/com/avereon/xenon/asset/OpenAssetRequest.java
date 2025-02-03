package com.avereon.xenon.asset;

import com.avereon.util.UriUtil;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.ToolManager;
import com.avereon.xenon.workpane.WorkpaneView;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Map;

/**
 * The OpenAssetRequest holds all the metadata needed to open an asset, and
 * possibly a tool along with it. This class is used by the {@link AssetManager}
 * and by the {@link ToolManager}. Because there is so much potential data, it
 * has been consolidated into this class. It is in turn passed on to the
 * {@link ProgramTool} to allow tool implementations to complete the request in
 * both the ready() and open() methods.
 *
 * @see AssetManager
 * @see ToolManager
 */
public class OpenAssetRequest {

	@Getter
	private AssetType type;

	@Getter
	private URI uri;

	@Getter
	private Codec codec;

	private Object model;

	@Getter
	private WorkpaneView view;

	/**
	 * Should a tool be opened for this request.
	 */
	@Getter
	private boolean openTool;

	/**
	 * Should the tool be set as the active tool if a tool is opened.
	 */
	@Getter
	private boolean setActive;

	/**
	 * The asset created from the asset type and URI
	 */
	@Getter
	private Asset asset;

	/**
	 * The tool id if restoring a tool.
	 */
	@Getter
	@Setter
	private String toolId;

	/**
	 * The resolved tool class if restoring a tool.
	 */
	@Getter
	@Setter
	private Class<? extends ProgramTool> toolClass;

	@Getter
	@Setter
	private String toolClassName;

	public OpenAssetRequest setUri( URI uri ) {
		this.uri = uri;
		return this;
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

	public Map<String,String> getQueryParameters() {
		return uri == null ? null : UriUtil.parseQuery( uri.getQuery() );
	}

	public String getFragment() {
		return uri == null ? null : UriUtil.parseFragment( uri );
	}

	public OpenAssetRequest setCodec( Codec codec ) {
		this.codec = codec;
		return this;
	}

	public OpenAssetRequest setView( WorkpaneView view ) {
		this.view = view;
		return this;
	}

	public OpenAssetRequest setOpenTool( boolean openTool ) {
		this.openTool = openTool;
		return this;
	}

	public OpenAssetRequest setSetActive( boolean setActive ) {
		this.setActive = setActive;
		return this;
	}

	public OpenAssetRequest setAsset( Asset asset ) {
		this.asset = asset;
		return this;
	}

}
