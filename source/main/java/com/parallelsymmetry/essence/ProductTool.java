package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.worktool.Tool;

import java.util.Collections;
import java.util.Set;

public class ProductTool extends Tool {

	private Product product;

	public ProductTool( Product product, Resource resource ) {
		super( resource );
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public Set<Class<? extends ProductTool>> getToolDependencies() {
		return Collections.unmodifiableSet( Collections.emptySet() );
	}

}
