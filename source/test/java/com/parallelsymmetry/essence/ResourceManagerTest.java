package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.resource.*;
import com.parallelsymmetry.essence.resource.event.ResourceClosedEvent;
import com.parallelsymmetry.essence.resource.event.ResourceLoadedEvent;
import com.parallelsymmetry.essence.resource.event.ResourceOpenedEvent;
import com.parallelsymmetry.essence.resource.event.ResourceSavedEvent;
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
	public void testNewResource() throws Exception {
		// New resources have a resource type but not a URI
		Resource oldResource = manager.createResource( manager.getResourceType( "mock" ) );
		assertThat( oldResource.isNew(), is( true ) );
	}

	@Test
	public void testOldResource() throws Exception {
		// Old resources have a URI
		String uri = "mock:///home/user/temp/test.txt";
		Resource oldResource = manager.createResource( uri );
		assertThat( oldResource.isNew(), is( false ) );
	}

	@Test
	public void testCreateResourceWithUri() {
		URI uri = URI.create( "mock:///home/user/temp/test.txt" );
		Resource resource = manager.createResource( uri );
		assertThat( resource.getScheme(), is( manager.getScheme( "mock" ) ) );
		assertThat( resource.getUri(), is( uri ) );
		assertThat( resource.isOpen(), is( false ) );
	}

	@Test
	public void testCreateResourceWithString() {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		assertThat( resource.getScheme(), is( manager.getScheme( "mock" ) ) );
		assertThat( resource.getUri(), is( URI.create( uri ) ) );
		assertThat( resource.isOpen(), is( false ) );
	}

	@Test
	public void testOpenResources() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		assertThat( resource.isOpen(), is( false ) );

		manager.openResources( resource );
		watcher.waitForEvent( ResourceOpenedEvent.class );
		assertThat( resource.isOpen(), is( true ) );
	}

	@Test
	public void testOpenResourcesAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		assertThat( resource.isOpen(), is( false ) );

		manager.openResourcesAndWait( resource );
		assertThat( resource.isOpen(), is( true ) );
	}

	@Test
	public void testLoadResources() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		assertThat( resource.isLoaded(), is( false ) );

		manager.loadResources( resource );
		watcher.waitForEvent( ResourceLoadedEvent.class );
		assertThat( resource.isOpen(), is( true ) );
		assertThat( resource.isLoaded(), is( true ) );
	}

	@Test
	public void testLoadResourcesAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		assertThat( resource.isLoaded(), is( false ) );

		manager.loadResourcesAndWait( resource );
		assertThat( resource.isOpen(), is( true ) );
		assertThat( resource.isLoaded(), is( true ) );
	}

	@Test
	public void testSaveResources() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		assertThat( resource.isSaved(), is( false ) );

		// Resource must be open to be saved
		manager.openResources( resource );
		watcher.waitForEvent( ResourceOpenedEvent.class );
		assertThat( resource.isOpen(), is( true ) );

		manager.saveResources( resource );
		watcher.waitForEvent( ResourceSavedEvent.class );
		assertThat( resource.isSaved(), is( true ) );
	}

	@Test
	public void testSaveResourcesAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );
		assertThat( resource.isSaved(), is( false ) );

		// Resource must be open to be saved
		manager.openResourcesAndWait( resource );
		assertThat( resource.isOpen(), is( true ) );

		manager.saveResourcesAndWait( resource );
		assertThat( resource.isSaved(), is( true ) );
	}

	@Test
	public void testCloseResources() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );

		// Resource must be open to be closed
		manager.openResources( resource );
		watcher.waitForEvent( ResourceOpenedEvent.class );
		assertThat( resource.isOpen(), is( true ) );

		manager.closeResources( resource );
		watcher.waitForEvent( ResourceClosedEvent.class );
		assertThat( resource.isOpen(), is( false ) );
	}

	@Test
	public void testCloseResourcesAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createResource( uri );
		ResourceWatcher watcher = new ResourceWatcher();
		resource.addResourceListener( watcher );

		// Resource must be open to be closed
		manager.openResourcesAndWait( resource );
		assertThat( resource.isOpen(), is( true ) );

		manager.closeResourcesAndWait( resource );
		assertThat( resource.isOpen(), is( false ) );
	}

	@Test
	public void testAutoDetectResourceTypeWithOpaqueUri() {
		Resource resource = manager.createResource( URI.create( "mock:test" ) );
		ResourceType type = manager.autoDetectResourceType( resource );
		assertThat( resource.getType(), instanceOf( MockResourceType.class ) );
	}

}
