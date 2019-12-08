package com.avereon.xenon.tool;

import com.avereon.settings.Settings;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Tool;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public abstract class ProgramTool extends Tool {

	private ProgramProduct product;

	private String uid;

	public ProgramTool( ProgramProduct product, Asset asset ) {
		super( asset );
		this.product = product;
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

}
