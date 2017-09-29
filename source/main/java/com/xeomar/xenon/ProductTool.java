package com.xeomar.xenon;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.worktool.Tool;

import java.util.Collections;
import java.util.Set;

public abstract class ProductTool extends Tool {

	private Product product;

	public ProductTool( Product product, Resource resource ) {
		super( resource );
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public Set<String> getResourceDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
	}

}
