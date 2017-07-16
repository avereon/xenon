package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.product.ProductBundle;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.Module;

public class MockModule extends Module {

	public MockModule( Program program, ProductMetadata card ) {
		super( program, card );
	}

	@Override
	public void register() {}

	@Override
	public void create() {}

	@Override
	public void destroy() {}

	@Override
	public void unregister() {}

	@Override
	public ProductBundle getResourceBundle() {
		return null;
	}

}
