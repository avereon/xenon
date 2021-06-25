package com.avereon.xenon.test.asset;

import com.avereon.xenon.test.ProgramTestCase;
import com.avereon.xenon.asset.*;
import com.avereon.xenon.scheme.NewScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AssetManagerTest extends ProgramTestCase {

	private AssetManager manager;

	@BeforeEach
	@Override
	protected void setup() throws Exception {
		super.setup();
		manager = new AssetManager( program );
		manager.addScheme( new MockScheme( program ) );
		manager.addScheme( new NewScheme( program ) );
		manager.addAssetType( new MockAssetType( program ) );
	}

	@Test
	void testNewAsset() throws Exception {
		// New assets have a asset type when created.
		// The URI is assigned when the asset is saved.
		Asset newAsset = manager.createAsset( manager.getAssetType( MockScheme.ID ) );
		assertThat( newAsset.isNew(), is( true ) );
	}

	@Test
	void testOldAsset() throws Exception {
		// Old assets have a URI when created.
		// The asset type is assigned when the asset is opened.
		String uri = "mock:///home/user/temp/test.txt";
		Asset oldAsset = manager.createAsset( uri );
		assertThat( oldAsset.isNew(), is( false ) );
	}

	@Test
	void testCreateAssetWithUri() throws Exception {
		URI uri = URI.create( "mock:///home/user/temp/test.txt" );
		Asset asset = manager.createAsset( uri );
		assertThat( asset.getScheme(), is( manager.getScheme( MockScheme.ID ) ) );
		assertThat( asset.getUri(), is( uri ) );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testCreateAssetWithString() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		assertThat( asset.getScheme(), is( manager.getScheme( MockScheme.ID ) ) );
		assertThat( asset.getUri(), is( URI.create( uri ) ) );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testOpenAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isOpen(), is( false ) );

		manager.openAssets( asset );
		watcher.waitForEvent( AssetEvent.OPENED );
		assertThat( asset.isOpen(), is( true ) );
	}

	@Test
	void testOpenAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isOpen(), is( false ) );

		manager.openAssetsAndWait( asset );
		assertThat( asset.isOpen(), is( true ) );
	}

	@Test
	void testLoadAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isLoaded(), is( false ) );

		manager.loadAssets( asset );
		watcher.waitForEvent( AssetEvent.LOADED );
		assertThat( asset.isOpen(), is( true ) );
		assertThat( asset.isLoaded(), is( true ) );
	}

	@Test
	void testLoadAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isLoaded(), is( false ) );

		manager.loadAssetsAndWait( asset );
		assertThat( asset.isOpen(), is( true ) );
		assertThat( asset.isLoaded(), is( true ) );
	}

	@Test
	void testSaveAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isSaved(), is( false ) );

		// Asset must be open to be saved
		manager.openAssets( asset );
		watcher.waitForEvent( AssetEvent.OPENED );
		assertThat( asset.isOpen(), is( true ) );

		manager.saveAssets( asset );
		watcher.waitForEvent( AssetEvent.SAVED );
		assertThat( asset.isSaved(), is( true ) );
	}

	@Test
	void testSaveAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );
		assertThat( asset.isSaved(), is( false ) );

		// Asset must be open to be saved
		manager.openAssetsAndWait( asset );
		assertThat( asset.isOpen(), is( true ) );

		manager.saveAssetsAndWait( asset );
		assertThat( asset.isSaved(), is( true ) );
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
		assertThat( asset.isOpen(), is( true ) );

		manager.closeAssets( asset );
		watcher.waitForEvent( AssetEvent.CLOSED );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testCloseAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.getEventHub().register( AssetEvent.ANY, watcher );

		// Asset must be open to be closed
		manager.openAssetsAndWait( asset );
		assertThat( asset.isOpen(), is( true ) );

		manager.closeAssetsAndWait( asset );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testAutoDetectAssetTypeWithOpaqueUri() throws Exception {
		Asset asset = manager.createAsset( URI.create( "mock:test" ) );
		manager.autoDetectAssetType( asset );
		assertThat( asset.getType(), instanceOf( MockAssetType.class ) );
	}

	@Test
	void testAutoDetectCodecs() throws Exception {
		AssetType type = manager.getAssetType( new MockAssetType( program ).getKey() );
		Asset asset = manager.createAsset( URI.create( "mock:test.mock" ) );
		Set<Codec> codecs = manager.autoDetectCodecs( asset );
		assertThat( codecs, equalTo( type.getCodecs() ) );
	}

}
