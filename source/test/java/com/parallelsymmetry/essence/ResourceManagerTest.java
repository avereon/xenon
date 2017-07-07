package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceType;
import com.parallelsymmetry.essence.resource.Scheme;
import com.parallelsymmetry.essence.resource.type.ProductInfoType;
import com.parallelsymmetry.essence.scheme.ProgramScheme;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest extends ProgramTestCase {

	private ResourceManager manager;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		manager = new ResourceManager( program );
	}

	@Test
	public void testResourceTypeLookupWithOpaqueUri() {
		Scheme scheme = new ProgramScheme( program );
		manager.addScheme( scheme );
		manager.registerUriResourceType( "program:about", new ProductInfoType( program ) );
		Resource resource = manager.createResource( URI.create( "program:about" ) );
		ResourceType type = manager.autoDetectResourceType( resource );

		assertThat( resource.getType(), instanceOf( ProductInfoType.class ) );
	}

}
