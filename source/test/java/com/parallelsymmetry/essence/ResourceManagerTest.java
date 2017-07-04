package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.ProgramResourceType;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceType;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest extends ProgramTestCase {

	private ResourceManager manager;

	@Before
	public void setup() throws Exception {
		super.setup();
		manager = program.getResourceManager();
	}

	@Test
	public void testResourceTypeLookup() {
		Resource resource = manager.createResource( URI.create( "program:about" ) );
		ResourceType type = manager.autoDetectResourceType( resource );

		assertThat( resource.getType(), not( is( nullValue() ) ));
		assertThat( resource.getType(), instanceOf( ProgramResourceType.class ) );
	}

}
