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

public class AssetManagerTest extends ProgramTestCase {

	private AssetManager manager;

	@BeforeEach
	@Override
	protected void setup() throws Exception {
		super.setup();
		manager = new AssetManager( getProgram() );
		manager.addScheme( new MockScheme( getProgram() ) );
		manager.addScheme( new NewScheme( getProgram() ) );
		manager.addAssetType( new MockAssetType( getProgram() ) );
	}

	@Test
	void testGetNullAssetType() {
		assertThat( manager.getAssetType( null ) ).isNull();
	}

	@Test
	void testNewAsset() throws Exception {
		// New assets have an asset type when created.
		// The URI is assigned when the asset is saved.
		Asset newAsset = manager.createAsset( manager.getAssetType( MockScheme.ID ) );
		assertThat( newAsset.isNew() ).isTrue();
	}

	@Test
	void testOldAsset() throws Exception {
		// Old assets have a URI when created.
		// The asset type is assigned when the asset is opened.
		String uri = "mock:///home/user/temp/test.txt";
		Asset oldAsset = manager.createAsset( uri );
		assertThat( oldAsset.isNew() ).isFalse();
	}

	@Test
	void testCreateAssetWithUri() throws Exception {
		URI uri = URI.create( "mock:///home/user/temp/test.txt" );
		Asset asset = manager.createAsset( uri );
		assertThat( asset.getScheme() ).isEqualTo( manager.getScheme( MockScheme.ID ) );
		assertThat( asset.getUri() ).isEqualTo( uri );
		assertThat( asset.isOpen() ).isFalse();
	}

	@Test
	void testCreateAssetWithString() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		assertThat( asset.getScheme() ).isEqualTo( manager.getScheme( MockScheme.ID ) );
		assertThat( asset.getUri() ).isEqualTo( URI.create( uri ) );
		assertThat( asset.isOpen() ).isFalse();
	}

	@Test
	void testOpenAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isOpen() ).isFalse();

		manager.openAssets( asset );
		watcher.waitForEvent( AssetEvent.OPENED );
		assertThat( asset.isOpen() ).isTrue();
	}

	@Test
	void testOpenAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isOpen() ).isFalse();

		manager.openAssetsAndWait( asset, 1, TimeUnit.SECONDS );
		assertThat( asset.isOpen() ).isTrue();
	}

	@Test
	void testLoadAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isLoaded() ).isFalse();

		manager.loadAssets( asset );
		watcher.waitForEvent( AssetEvent.LOADED );
		assertThat( asset.isOpen() ).isTrue();
		assertThat( asset.isLoaded() ).isTrue();
	}

	@Test
	void testLoadAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isLoaded() ).isFalse();

		manager.loadAssetsAndWait( asset );
		assertThat( asset.isOpen() ).isTrue();
		assertThat( asset.isLoaded() ).isTrue();
	}

	@Test
	void testReloadAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isLoaded() ).isFalse();

		manager.loadAssetsAndWait( asset );
		assertThat( asset.isOpen() ).isTrue();
		assertThat( asset.isLoaded() ).isTrue();
		assertThat( watcher.getLastEvent().getEventType() ).isEqualTo( AssetEvent.LOADED );
		AssetEvent event = watcher.getLastEvent();

		manager.reloadAssetsAndWait( asset );
		assertThat( watcher.getLastEvent().getEventType() ).isEqualTo( AssetEvent.LOADED );
		assertThat( event ).isNotEqualTo( watcher.getLastEvent() );
	}

	@Test
	void testSaveAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isSaved() ).isFalse();

		// Asset must be open to be saved
		manager.openAssets( asset );
		watcher.waitForEvent( AssetEvent.OPENED );
		assertThat( asset.isOpen() ).isTrue();

		// Asset must be loaded to be saved
		manager.loadAssets( asset );
		watcher.waitForEvent( AssetEvent.LOADED );
		assertThat( asset.isLoaded() ).isTrue();

		// And an asset must be modified to be saved
		asset.setModified( true );
		assertThat( asset.isSafeToSave() ).isTrue();

		manager.saveAssets( asset );
		watcher.waitForEvent( AssetEvent.SAVED );
		assertThat( asset.isSaved() ).isTrue();
	}

	@Test
	void testSaveAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isSaved() ).isFalse();

		// Asset must be open to be saved
		manager.openAssetsAndWait( asset, 1, TimeUnit.SECONDS );
		assertThat( asset.isOpen() ).isTrue();

		// Asset must be loaded to be saved
		manager.loadAssets( asset );
		watcher.waitForEvent( AssetEvent.LOADED );
		assertThat( asset.isLoaded() ).isTrue();

		// And an asset must be modified to be saved
		asset.setModified( true );
		assertThat( asset.isSafeToSave() ).isTrue();

		manager.saveAssetsAndWait( asset );
		assertThat( asset.isSaved() ).isTrue();
	}

	@Test
	void testCloseAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );

		// Asset must be open to be closed
		manager.openAssets( asset );
		watcher.waitForEvent( AssetEvent.OPENED );
		assertThat( asset.isOpen() ).isTrue();

		manager.closeAssets( asset );
		watcher.waitForEvent( AssetEvent.CLOSED );
		assertThat( asset.isOpen() ).isFalse();
	}

	@Test
	void testCloseAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );

		// Asset must be open to be closed
		manager.openAssetsAndWait( asset, 1, TimeUnit.SECONDS );
		assertThat( asset.isOpen() ).isTrue();

		manager.closeAssetsAndWait( asset );
		assertThat( asset.isOpen() ).isFalse();
	}

	@Test
	void testAutoDetectAssetTypeWithOpaqueUri() throws Exception {
		Asset asset = manager.createAsset( URI.create( "mock:test" ) );
		manager.autoDetectAssetType( asset );
		assertThat( asset.getType() ).isInstanceOf( MockAssetType.class );
	}

	@Test
	void testAutoDetectCodecs() throws Exception {
		AssetType type = manager.getAssetType( new MockAssetType( getProgram() ).getKey() );
		Asset asset = manager.createAsset( URI.create( "mock:test.mock" ) );
		Set<Codec> codecs = manager.autoDetectCodecs( asset );
		assertThat( codecs ).isEqualTo( type.getCodecs() );
	}

	@Test
	void canRenameAssetWithNull() {
		assertThat( manager.canRenameAsset( null ) ).isFalse();
	}

	@Test
	void canRenameAssetWithNewAsset() throws Exception {
		Asset asset = manager.createAsset( manager.getAssetType( FileScheme.ID ), "mock://test.mock" );
		assertThat( manager.canRenameAsset( asset ) ).isFalse();
	}

	@Test
	void canRenameAssetWithOldAsset() throws Exception {
		Asset asset = manager.createAsset( "mock://test.mock" );
		manager.openAssetsAndWait( asset, 100, TimeUnit.MILLISECONDS );
		assertThat( manager.canRenameAsset( asset ) ).isTrue();
	}

	@Test
	void cleanupUri() {
		URI provided = URI.create( "mock:///home/user/temp/test.txt?param1=one&param2=two#readwrite" );
		URI expected = URI.create( "mock:/home/user/temp/test.txt" );
		assertThat( manager.uriCleanup( provided ) ).isEqualTo( expected );
	}

}
