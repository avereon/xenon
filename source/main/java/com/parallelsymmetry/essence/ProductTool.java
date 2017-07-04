package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.workspace.ToolInstanceMode;
import com.parallelsymmetry.essence.worktool.Tool;

public class ProductTool extends Tool {

	private Product product;

	public ProductTool( Product product, Resource resource ) {
		super( resource );
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public ToolInstanceMode getInstanceMode() {
		return ToolInstanceMode.UNLIMITED;
	}

}
