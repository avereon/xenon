package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.*;
import com.parallelsymmetry.essence.resource.type.ProductInfoType;
import com.parallelsymmetry.essence.scheme.ProgramScheme;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResourceManagerTest extends ProgramTestCase {

	private ResourceManager manager;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		manager = new ResourceManager( program );
		manager.addScheme( new MockScheme( program ) );
		manager.registerSchemeResourceType( "mock", new MockResourceType( program ) );
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

	@Test
	public void testCreateResource() {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		assertThat( resource.getScheme(), is( manager.getScheme( "mock" ) ) );
		assertThat( resource.getUri(), is( URI.create( uri ) ) );
		assertThat( resource.isOpen(), is( false ) );
	}

	@Test
	public void testOpenResource() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		manager.openResources( resource );
		//assertThat( resource.isOpen(), is( false ) );

		watcher.waitForEvent( ResourceEvent.class );
		assertThat( resource.isOpen(), is( true ) );
	}

	@Test
	public void testLoadResource() {

	}

	@Test
	public void testSaveResource() {

	}

	@Test
	public void testCloseResource() {

	}

}
