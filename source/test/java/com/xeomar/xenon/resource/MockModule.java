package com.xeomar.xenon.resource;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.product.ProductBundle;
import com.xeomar.xenon.product.ProductMetadata;
import com.xeomar.xenon.Module;

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
