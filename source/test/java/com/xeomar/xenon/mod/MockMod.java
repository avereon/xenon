package com.xeomar.xenon.mod;

import com.xeomar.product.ProductBundle;

public class MockMod extends Mod {

	public MockMod() {
		super();
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
