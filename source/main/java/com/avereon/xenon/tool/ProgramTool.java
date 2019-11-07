package com.avereon.xenon.tool;

import com.avereon.settings.Settings;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.workarea.Tool;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public abstract class ProgramTool extends Tool {

	private ProgramProduct product;

	public ProgramTool( ProgramProduct product, Resource resource ) {
		super( resource );
		this.product = product;
		setCloseGraphic( product.getProgram().getIconLibrary().getIcon( "workarea-close" ) );
	}

	public ProgramProduct getProduct() {
		return product;
	}

	public Program getProgram() {
		return product.getProgram();
	}

	public Set<URI> getResourceDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
	}

	public Settings getSettings() {
		return getProgram().getSettingsManager().getSettings( ProgramSettings.TOOL, getId() );
	}

}
