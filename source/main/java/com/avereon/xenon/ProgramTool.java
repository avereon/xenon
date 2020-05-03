package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Tool;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public abstract class ProgramTool extends Tool {

	private static final System.Logger log = Log.get();

	private ProgramProduct product;

	private String uid;

	public ProgramTool( ProgramProduct product, Asset asset ) {
		super( asset );
		this.product = product;
		setTitle( getAsset().getName() );
		setCloseGraphic( product.getProgram().getIconLibrary().getIcon( "workarea-close" ) );
	}

	public ProgramProduct getProduct() {
		return product;
	}

	public Program getProgram() {
		return product.getProgram();
	}

	public Set<URI> getAssetDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
	}

	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, getUid() );
	}

	public String getUid() {
		return uid;
	}

	public void setUid( String uid ) {
		this.uid = uid;
	}

	@Override
	public void close() {
		Set<Tool> tools = getProgram().getWorkspaceManager().getAssetTools( getAsset() );
		if( !tools.contains( this ) ) return;

		if( getAsset().isNewOrModified() ) {
			getProgram().getWorkspaceManager().handleModifiedAssets( ProgramScope.TOOL, Set.of( getAsset() ) );
		} else if( tools.size() == 1 ) {
			getProgram().getAssetManager().close( getAsset() );
		} else {
			super.close();
		}
	}

	protected void pushToolActions( String... actions ) {
		getProgram().getWorkspaceManager().getActiveWorkspace().pushToolbarActions( actions );
	}

	protected void pullToolActions() {
		getProgram().getWorkspaceManager().getActiveWorkspace().pullToolbarActions();
	}

	protected ProgramTool pushAction( String key, Action action ) {
		getProgram().getActionLibrary().getAction( key ).pushAction( action );
		return this;
	}

	protected ProgramTool pullAction( String key, Action action ) {
		getProgram().getActionLibrary().getAction( key ).pullAction( action );
		return this;
	}

}
