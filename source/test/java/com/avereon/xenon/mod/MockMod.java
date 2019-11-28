package com.avereon.xenon.mod;

import com.avereon.product.ProductBundle;
import com.avereon.xenon.Mod;

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
	public ProductBundle rb() {
		return null;
	}

}
