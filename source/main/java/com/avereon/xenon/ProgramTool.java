package com.avereon.xenon;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public abstract class ProgramTool extends Tool {

	private static final System.Logger log = Log.log();

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

	protected ProgramTool pushAction( String key, Action action ) {
		getProgram().getActionLibrary().getAction( key ).pushAction( action );
		return this;
	}

	protected ProgramTool pullAction( String key, Action action ) {
		getProgram().getActionLibrary().getAction( key ).pullAction( action );
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void callAssetRefreshed() {
		Platform.runLater( () -> {
			try {
				assetRefreshed();
			} catch( ToolException exception ) {
				log.log( Log.ERROR, "Error refreshing tool", exception );
			}
		} );
	}

}
