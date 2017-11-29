package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.Tool;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public abstract class AbstractTool extends Tool {

	private Product product;

	public AbstractTool( Product product, Resource resource ) {
		super( resource );
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public Set<URI> getResourceDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
	}

}
