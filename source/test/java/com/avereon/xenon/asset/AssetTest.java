package com.avereon.xenon.asset;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AssetTest {

	@Test
	void testGetNameWithOpaqueUri() {
		Asset asset = new Asset( URI.create( "opaque:name" ) );
		assertThat( asset.getName(), is( "opaque:name" ) );
	}

	@Test
	void testGetNameWithAbsoluteUri() {
		Asset asset = new Asset( URI.create( "file:/path/to/file/file.txt" ) );
		assertThat( asset.getName(), is( "file.txt" ) );
	}

	@Test
	void testGetNameWithRelativeUri() {
		Asset asset = new Asset( URI.create( "path/to/file/file.txt" ) );
		assertThat( asset.getName(), is( "file.txt" ) );
	}

	@Test
	void testGetNameWithNameOnlyUri() {
		Asset asset = new Asset( URI.create( "file.txt" ) );
		assertThat( asset.getName(), is( "file.txt" ) );
	}

	@Test
	void testGetNameWithRootUri() {
		Asset asset = new Asset( URI.create( "/" ) );
		assertThat( asset.getName(), is( "/" ) );
	}

	@Test
	void testGetNameWithFolderUri() {
		Asset asset = new Asset( URI.create( "/home/test/" ) );
		assertThat( asset.getName(), is( "test" ) );
	}

	@Test
	void testGetFileNameWithOpaqueUri() {
		Asset asset = new Asset( URI.create( "opaque:name" ) );
		assertThat( asset.getFileName(), is( "name" ) );
	}

	@Test
	void testGetFileNameWithAbsoluteUri() {
		Asset asset = new Asset( URI.create( "file:/path/to/file/file.txt" ) );
		assertThat( asset.getFileName(), is( "file.txt" ) );
	}

	@Test
	void testGetFileNameWithRelativeUri() {
		Asset asset = new Asset( URI.create( "path/to/file/file.txt" ) );
		assertThat( asset.getFileName(), is( "file.txt" ) );
	}

	@Test
	void testGetFileNameWithNameOnlyUri() {
		Asset asset = new Asset( URI.create( "file.txt" ) );
		assertThat( asset.getFileName(), is( "file.txt" ) );
	}

	@Test
	void testGetFileNameWithRootUri() {
		Asset asset = new Asset( URI.create( "/" ) );
		assertThat( asset.getFileName(), is( "/" ) );
	}

	@Test
	void testGetFileNameWithFolderUri() {
		Asset asset = new Asset( URI.create( "/home/test/" ) );
		assertThat( asset.getFileName(), is( "test" ) );
	}

}
