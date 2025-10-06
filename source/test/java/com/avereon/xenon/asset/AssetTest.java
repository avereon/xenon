package com.avereon.xenon.asset;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssetTest extends BaseAssetTestCase {

	@Test
	void testGetNameWithOpaqueUri() {
		Asset asset = new Asset( URI.create( "opaque:name" ) );
		assertThat( asset.getName() ).isEqualTo( "opaque:name" );
	}

	@Test
	void testGetNameWithAbsoluteUri() {
		Asset asset = new Asset( URI.create( "file:/path/to/file/file.txt" ) );
		assertThat( asset.getName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetNameWithRelativeUri() {
		Asset asset = new Asset( URI.create( "path/to/file/file.txt" ) );
		assertThat( asset.getName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetNameWithNameOnlyUri() {
		Asset asset = new Asset( URI.create( "file.txt" ) );
		assertThat( asset.getName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetNameWithRootUri() {
		Asset asset = new Asset( URI.create( "/" ) );
		assertThat( asset.getName() ).isEqualTo( "/" );
	}

	@Test
	void testGetNameWithFolderUri() {
		Asset asset = new Asset( URI.create( "/home/test/" ) );
		assertThat( asset.getName() ).isEqualTo( "test" );
	}

	@Test
	void testGetFileNameWithOpaqueUri() {
		Asset asset = new Asset( URI.create( "opaque:name" ) );
		assertThat( asset.getFileName() ).isEqualTo( "name" );
	}

	@Test
	void testGetFileNameWithAbsoluteUri() {
		Asset asset = new Asset( URI.create( "file:/path/to/file/file.txt" ) );
		assertThat( asset.getFileName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetFileNameWithRelativeUri() {
		Asset asset = new Asset( URI.create( "path/to/file/file.txt" ) );
		assertThat( asset.getFileName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetFileNameWithNameOnlyUri() {
		Asset asset = new Asset( URI.create( "file.txt" ) );
		assertThat( asset.getFileName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetFileNameWithRootUri() {
		Asset asset = new Asset( URI.create( "/" ) );
		assertThat( asset.getFileName() ).isEqualTo( "/" );
	}

	@Test
	void testGetFileNameWithFolderUri() {
		Asset asset = new Asset( URI.create( "/home/test/" ) );
		assertThat( asset.getFileName() ).isEqualTo( "test" );
	}

	@Test
	void isNew() {
		Asset asset = new Asset( new MockResourceType( getProduct() ), URI.create( "new:" + UUID.randomUUID() ) );
		assertThat( asset.isNew() ).isTrue();
	}

}
