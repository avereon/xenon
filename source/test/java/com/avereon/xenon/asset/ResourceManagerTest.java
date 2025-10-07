package com.avereon.xenon.asset;

import com.avereon.xenon.ProgramTestCase;
import com.avereon.xenon.scheme.FileScheme;
import com.avereon.xenon.scheme.NewScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceManagerTest extends ProgramTestCase {

	private ResourceManager manager;

	@BeforeEach
	@Override
	protected void setup() throws Exception {
		super.setup();
		manager = new ResourceManager( getProgram() );
		manager.addScheme( new MockScheme( getProgram() ) );
		manager.addScheme( new NewScheme( getProgram() ) );
		manager.addAssetType( new MockResourceType( getProgram() ) );
	}

	@Test
	void testGetNullAssetType() {
		assertThat( manager.getAssetType( null ) ).isNull();
	}

	@Test
	void testNewAsset() throws Exception {
		// New assets have an asset type when created.
		// The URI is assigned when the asset is saved.
		Resource newResource = manager.createAsset( manager.getAssetType( MockScheme.ID ) );
		assertThat( newResource.isNew() ).isTrue();
	}

	@Test
	void testOldAsset() throws Exception {
		// Old assets have a URI when created.
		// The asset type is assigned when the asset is opened.
		String uri = "mock:///home/user/temp/test.txt";
		Resource oldResource = manager.createAsset( uri );
		assertThat( oldResource.isNew() ).isFalse();
	}

	@Test
	void testCreateAssetWithUri() throws Exception {
		URI uri = URI.create( "mock:///home/user/temp/test.txt" );
		Resource resource = manager.createAsset( uri );
		assertThat( resource.getScheme() ).isEqualTo( manager.getScheme( MockScheme.ID ) );
		assertThat( resource.getUri() ).isEqualTo( uri );
		assertThat( resource.isOpen() ).isFalse();
	}

	@Test
	void testCreateAssetWithString() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		assertThat( resource.getScheme() ).isEqualTo( manager.getScheme( MockScheme.ID ) );
		assertThat( resource.getUri() ).isEqualTo( URI.create( uri ) );
		assertThat( resource.isOpen() ).isFalse();
	}

	@Test
	void testOpenAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isOpen() ).isFalse();

		manager.openAssets( resource );
		watcher.waitForEvent( ResourceEvent.OPENED );
		assertThat( resource.isOpen() ).isTrue();
	}

	@Test
	void testOpenAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isOpen() ).isFalse();

		manager.openAssetsAndWait( resource, 1, TimeUnit.SECONDS );
		assertThat( resource.isOpen() ).isTrue();
	}

	@Test
	void testLoadAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isLoaded() ).isFalse();

		manager.loadAssets( resource );
		watcher.waitForEvent( ResourceEvent.LOADED );
		assertThat( resource.isOpen() ).isTrue();
		assertThat( resource.isLoaded() ).isTrue();
	}

	@Test
	void testLoadAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isLoaded() ).isFalse();

		manager.loadAssetsAndWait( resource );
		assertThat( resource.isOpen() ).isTrue();
		assertThat( resource.isLoaded() ).isTrue();
	}

	@Test
	void testReloadAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isLoaded() ).isFalse();

		manager.loadAssetsAndWait( resource );
		assertThat( resource.isOpen() ).isTrue();
		assertThat( resource.isLoaded() ).isTrue();
		assertThat( watcher.getLastEvent().getEventType() ).isEqualTo( ResourceEvent.LOADED );
		ResourceEvent event = watcher.getLastEvent();

		manager.reloadAssetsAndWait( resource );
		assertThat( watcher.getLastEvent().getEventType() ).isEqualTo( ResourceEvent.LOADED );
		assertThat( event ).isNotEqualTo( watcher.getLastEvent() );
	}

	@Test
	void testSaveAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isSaved() ).isFalse();

		// Asset must be open to be saved
		manager.openAssets( resource );
		watcher.waitForEvent( ResourceEvent.OPENED );
		assertThat( resource.isOpen() ).isTrue();

		// Asset must be loaded to be saved
		manager.loadAssets( resource );
		watcher.waitForEvent( ResourceEvent.LOADED );
		assertThat( resource.isLoaded() ).isTrue();

		// And an asset must be modified to be saved
		resource.setModified( true );
		assertThat( resource.isSafeToSave() ).isTrue();

		manager.saveAssets( resource );
		watcher.waitForEvent( ResourceEvent.SAVED );
		assertThat( resource.isSaved() ).isTrue();
	}

	@Test
	void testSaveAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );
		assertThat( resource.isSaved() ).isFalse();

		// Asset must be open to be saved
		manager.openAssetsAndWait( resource, 1, TimeUnit.SECONDS );
		assertThat( resource.isOpen() ).isTrue();

		// Asset must be loaded to be saved
		manager.loadAssets( resource );
		watcher.waitForEvent( ResourceEvent.LOADED );
		assertThat( resource.isLoaded() ).isTrue();

		// And an asset must be modified to be saved
		resource.setModified( true );
		assertThat( resource.isSafeToSave() ).isTrue();

		manager.saveAssetsAndWait( resource );
		assertThat( resource.isSaved() ).isTrue();
	}

	@Test
	void testCloseAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );

		// Asset must be open to be closed
		manager.openAssets( resource );
		watcher.waitForEvent( ResourceEvent.OPENED );
		assertThat( resource.isOpen() ).isTrue();

		manager.closeAssets( resource );
		watcher.waitForEvent( ResourceEvent.CLOSED );
		assertThat( resource.isOpen() ).isFalse();
	}

	@Test
	void testCloseAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Resource resource = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		resource.getEventHub().register( ResourceEvent.ANY, watcher );

		// Asset must be open to be closed
		manager.openAssetsAndWait( resource, 1, TimeUnit.SECONDS );
		assertThat( resource.isOpen() ).isTrue();

		manager.closeAssetsAndWait( resource );
		assertThat( resource.isOpen() ).isFalse();
	}

	@Test
	void testAutoDetectAssetTypeWithOpaqueUri() throws Exception {
		Resource resource = manager.createAsset( URI.create( "mock:test" ) );
		manager.autoDetectAssetType( resource );
		assertThat( resource.getType() ).isInstanceOf( MockResourceType.class );
	}

	@Test
	void testAutoDetectCodecs() throws Exception {
		ResourceType type = manager.getAssetType( new MockResourceType( getProgram() ).getKey() );
		Resource resource = manager.createAsset( URI.create( "mock:test.mock" ) );
		Set<Codec> codecs = manager.autoDetectCodecs( resource );
		assertThat( codecs ).isEqualTo( type.getCodecs() );
	}

	@Test
	void canRenameAssetWithNull() {
		assertThat( manager.canRenameAsset( null ) ).isFalse();
	}

	@Test
	void canRenameAssetWithNewAsset() throws Exception {
		Resource resource = manager.createAsset( manager.getAssetType( FileScheme.ID ), "mock://test.mock" );
		assertThat( manager.canRenameAsset( resource ) ).isFalse();
	}

	@Test
	void canRenameAssetWithOldAsset() throws Exception {
		Resource resource = manager.createAsset( "mock://test.mock" );
		manager.openAssetsAndWait( resource, 100, TimeUnit.MILLISECONDS );
		assertThat( manager.canRenameAsset( resource ) ).isTrue();
	}

	@Test
	void cleanupUri() {
		URI provided = URI.create( "mock:///home/user/temp/test.txt?param1=one&param2=two#readwrite" );
		URI expected = URI.create( "mock:/home/user/temp/test.txt" );
		assertThat( manager.uriCleanup( provided ) ).isEqualTo( expected );
	}

}
