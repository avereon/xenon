package com.xeomar.xenon.tool;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.Tool;

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

}
