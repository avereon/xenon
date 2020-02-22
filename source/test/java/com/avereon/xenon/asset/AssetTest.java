package com.avereon.xenon.asset;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AssetTest {

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

}
