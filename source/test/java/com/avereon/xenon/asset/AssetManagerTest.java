package com.avereon.xenon.asset;

import com.avereon.xenon.ProgramTestCase;
import com.avereon.xenon.asset.event.AssetClosedEvent;
import com.avereon.xenon.asset.event.AssetLoadedEvent;
import com.avereon.xenon.asset.event.AssetOpenedEvent;
import com.avereon.xenon.asset.event.AssetSavedEvent;
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
	public void setup() throws Exception {
		super.setup();
		manager = new AssetManager( program );
		manager.addScheme( new MockScheme( program ) );
		manager.registerSchemeAssetType( "mock", new MockAssetType( program ) );
	}

	@Test
	void testNewAsset() throws Exception {
		// New assets have a asset type when created.
		// The URI is assigned when the asset is saved.
		Asset newAsset = manager.createAsset( manager.getAssetType( "mock" ) );
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
		assertThat( asset.getScheme(), is( manager.getScheme( "mock" ) ) );
		assertThat( asset.getUri(), is( uri ) );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testCreateAssetWithString() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		assertThat( asset.getScheme(), is( manager.getScheme( "mock" ) ) );
		assertThat( asset.getUri(), is( URI.create( uri ) ) );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testOpenAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.addAssetListener( watcher );
		assertThat( asset.isOpen(), is( false ) );

		manager.openAssets( asset );
		watcher.waitForEvent( AssetOpenedEvent.class );
		assertThat( asset.isOpen(), is( true ) );
	}

	@Test
	void testOpenAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.addAssetListener( watcher );
		assertThat( asset.isOpen(), is( false ) );

		manager.openAssetsAndWait( asset );
		assertThat( asset.isOpen(), is( true ) );
	}

	@Test
	void testLoadAssets() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.addAssetListener( watcher );
		assertThat( asset.isLoaded(), is( false ) );

		manager.loadAssets( asset );
		watcher.waitForEvent( AssetLoadedEvent.class );
		assertThat( asset.isOpen(), is( true ) );
		assertThat( asset.isLoaded(), is( true ) );
	}

	@Test
	void testLoadAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.addAssetListener( watcher );
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
		asset.addAssetListener( watcher );
		assertThat( asset.isSaved(), is( false ) );

		// Asset must be open to be saved
		manager.openAssets( asset );
		watcher.waitForEvent( AssetOpenedEvent.class );
		assertThat( asset.isOpen(), is( true ) );

		manager.saveAssets( asset );
		watcher.waitForEvent( AssetSavedEvent.class );
		assertThat( asset.isSaved(), is( true ) );
	}

	@Test
	void testSaveAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.addAssetListener( watcher );
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
		asset.addAssetListener( watcher );

		// Asset must be open to be closed
		manager.openAssets( asset );
		watcher.waitForEvent( AssetOpenedEvent.class );
		assertThat( asset.isOpen(), is( true ) );

		manager.closeAssets( asset );
		watcher.waitForEvent( AssetClosedEvent.class );
		assertThat( asset.isOpen(), is( false ) );
	}

	@Test
	void testCloseAssetsAndWait() throws Exception {
		String uri = "mock:///home/user/temp/test.txt";
		Asset asset = manager.createAsset( uri );
		AssetWatcher watcher = new AssetWatcher();
		asset.addAssetListener( watcher );

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
		AssetType type = manager.getAssetType( "mock" );
		Asset asset = manager.createAsset( URI.create( "mock:test.mock" ) );
		Set<Codec> codecs = manager.autoDetectCodecs( asset );
		assertThat( codecs, equalTo( type.getCodecs() ) );
	}

	@Test
	void testToAssetUri() {
		assertThat( AssetManager.toAssetUri( URI.create( "program:product#update" ) ), is( URI.create( "program:product" ) ) );
		assertThat( AssetManager.toAssetUri( URI.create( "https://absolute/path?query" ) ), is( URI.create( "https://absolute/path" ) ) );
		assertThat( AssetManager.toAssetUri( URI.create( "/absolute/path?query#fragment" ) ), is( URI.create( "/absolute/path" ) ) );
		assertThat( AssetManager.toAssetUri( URI.create( "relative/path?query#fragment" ) ), is( URI.create( "relative/path" ) ) );
	}

}