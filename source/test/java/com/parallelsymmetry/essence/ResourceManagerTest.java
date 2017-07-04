package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceType;
import com.parallelsymmetry.essence.resource.Scheme;
import com.parallelsymmetry.essence.resource.type.ProductInfoType;
import com.parallelsymmetry.essence.scheme.ProgramScheme;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest extends BaseTestCase {

	private Program program;

	private ResourceManager manager;

	@Before
	public void setup() throws Exception {
		super.setup();
		program = new Program();
		manager = new ResourceManager( program );
	}

	@Test
	public void testResourceTypeLookup() {
		Scheme scheme = new ProgramScheme( program );
		manager.addScheme( scheme );
		manager.registerUriResourceType( "program:about", new ProductInfoType( program, "" ) );
		Resource resource = manager.createResource( URI.create( "program:about" ) );
		ResourceType type = manager.autoDetectResourceType( resource );

		assertThat( resource.getType(), not( is( nullValue() ) ) );
		assertThat( resource.getType(), instanceOf( ProductInfoType.class ) );
	}

}
