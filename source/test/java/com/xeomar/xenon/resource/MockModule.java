package com.xeomar.xenon.resource;

import com.xeomar.xenon.Module;
import com.xeomar.xenon.Program;
import com.xeomar.util.ProductBundle;
import com.xeomar.util.ProductMetadata;

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
	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	@Override
	public ProductBundle getResourceBundle() {
		return null;
	}

}
