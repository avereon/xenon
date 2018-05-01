package com.xeomar.xenon.resource;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.Module;
import com.xeomar.xenon.Program;
import com.xeomar.product.ProductBundle;

public class MockModule extends Module {

	public MockModule( Program program, ProductCard card ) {
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
	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	@Override
	public ProductBundle getResourceBundle() {
		return null;
	}

}
