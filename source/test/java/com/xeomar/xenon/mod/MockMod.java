package com.xeomar.xenon.mod;

import com.xeomar.product.ProductBundle;
import com.xeomar.xenon.Mod;

public class MockMod extends Mod {

	public MockMod() throws Exception {
		super();
	}

	@Override
	public void register() {}

	@Override
	public void startup() {}

	@Override
	public void shutdown() {}

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
