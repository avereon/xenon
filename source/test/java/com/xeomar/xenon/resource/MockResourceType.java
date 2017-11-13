package com.xeomar.xenon.resource;

import com.xeomar.xenon.Program;
import com.xeomar.util.Product;

public class MockResourceType extends ResourceType {

	private String key;

	private static final String NAME = "Mock Resource";

	private static final String DESCRIPTION = "Mock Resource Type";

	private static final String INIT_RESOURCE_KEY = "init.resource.key";

	private Codec defaultCodec;

	public MockResourceType( Product product ) {
		this( product, "mock", new MockCodec() );
	}

	public MockResourceType( Product product, String key, Codec defaultCodec ) {
		super( product, key );
		this.key = key;
		setDefaultCodec( defaultCodec );
	}

	@Override
	public String getKey() {
		return key == null ? super.getKey() : key;
	}

	@Override
	public String getName() {
		return NAME + " (" + getKey() + ")";
	}

	@Override
	public String getDescription() {
		return DESCRIPTION + " (" + getKey() + ")";
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) {
		resource.putResource( INIT_RESOURCE_KEY, "init.resource.test" );
		return true;
	}

}
