package com.parallelsymmetry.essence.resource;

import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.product.Product;

public class MockResourceType extends ResourceType {

	private static final String KEY = "mock";

	private static final String NAME = "Mock Resource";

	private static final String DESCRIPTION = "Mock Resource Type";

	private static final String INIT_RESOURCE_KEY = "init.resource.key";

	private Codec defaultCodec;

	public MockResourceType( Product product ) {
		super( product, "mock" );
		addCodec( defaultCodec = new MockCodec() );
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public Codec getDefaultCodec() {
		return defaultCodec;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) {
		resource.putResource( INIT_RESOURCE_KEY, "init.resource.test" );
		return true;
	}

}
