package com.xeomar.xenon.resource;

import com.xeomar.util.UriUtil;
import com.xeomar.xenon.ProgramTestCase;
import com.xeomar.xenon.resource.event.ResourceClosedEvent;
import com.xeomar.xenon.resource.event.ResourceLoadedEvent;
import com.xeomar.xenon.resource.event.ResourceOpenedEvent;
import com.xeomar.xenon.resource.event.ResourceSavedEvent;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
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
		// New resources have a resource type when created.
		// The URI is assigned when the resource is saved.
		Resource newResource = manager.createResource( manager.getResourceType( "mock" ) );
		assertThat( newResource.isNew(), is( true ) );
	}

	@Test
	public void testOldResource() throws Exception {
		// Old resources have a URI when created.
		// The resource type is assigned when the resource is opened.
		String uri = "mock:///home/user/temp/test.txt";
		Resource oldResource = manager.createResource( uri );
		assertThat( oldResource.isNew(), is( false ) );
	}

	@Test
	public void testCreateResourceWithUri() throws Exception {
		URI uri = URI.create( "mock:///home/user/temp/test.txt" );
		Resource resource = manager.createResource( uri );
		assertThat( resource.getScheme(), is( manager.getScheme( "mock" ) ) );
		assertThat( resource.getUri(), is( uri ) );
		assertThat( resource.isOpen(), is( false ) );
	}

	@Test
	public void testCreateResourceWithString() throws Exception {
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
	public void testAutoDetectResourceTypeWithOpaqueUri() throws Exception {
		Resource resource = manager.createResource( URI.create( "mock:test" ) );
		manager.autoDetectResourceType( resource );
		assertThat( resource.getType(), instanceOf( MockResourceType.class ) );
	}

	@Test
	public void testAutoDetectCodecs() throws Exception {
		ResourceType type = manager.getResourceType( "mock" );
		Resource resource = manager.createResource( URI.create( "mock:test.mock" ) );
		Set<Codec> codecs = manager.autoDetectCodecs( resource );
		assertThat( codecs, equalTo( type.getCodecs() ) );
	}

	@Test
	public void testToResourceUri() throws Exception {
			assertThat( ResourceManager.toResourceUri( URI.create( "program:product#update" ) ), is( URI.create( "program:product" ) ) );
			assertThat( ResourceManager.toResourceUri( URI.create( "https://absolute/path?query" ) ), is( URI.create( "https://absolute/path" ) ) );
			assertThat( ResourceManager.toResourceUri( URI.create( "/absolute/path?query#fragment" ) ), is( URI.create( "/absolute/path" ) ) );
			assertThat( ResourceManager.toResourceUri( URI.create( "relative/path?query#fragment" ) ), is( URI.create( "relative/path" ) ) );
	}

}
