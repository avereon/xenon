package com.avereon.xenon.asset;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceTest extends BaseResourceTestCase {

	@Test
	void testGetNameWithOpaqueUri() {
		Resource resource = new Resource( URI.create( "opaque:name" ) );
		assertThat( resource.getName() ).isEqualTo( "opaque:name" );
	}

	@Test
	void testGetNameWithAbsoluteUri() {
		Resource resource = new Resource( URI.create( "file:/path/to/file/file.txt" ) );
		assertThat( resource.getName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetNameWithRelativeUri() {
		Resource resource = new Resource( URI.create( "path/to/file/file.txt" ) );
		assertThat( resource.getName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetNameWithNameOnlyUri() {
		Resource resource = new Resource( URI.create( "file.txt" ) );
		assertThat( resource.getName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetNameWithRootUri() {
		Resource resource = new Resource( URI.create( "/" ) );
		assertThat( resource.getName() ).isEqualTo( "/" );
	}

	@Test
	void testGetNameWithFolderUri() {
		Resource resource = new Resource( URI.create( "/home/test/" ) );
		assertThat( resource.getName() ).isEqualTo( "test" );
	}

	@Test
	void testGetFileNameWithOpaqueUri() {
		Resource resource = new Resource( URI.create( "opaque:name" ) );
		assertThat( resource.getFileName() ).isEqualTo( "name" );
	}

	@Test
	void testGetFileNameWithAbsoluteUri() {
		Resource resource = new Resource( URI.create( "file:/path/to/file/file.txt" ) );
		assertThat( resource.getFileName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetFileNameWithRelativeUri() {
		Resource resource = new Resource( URI.create( "path/to/file/file.txt" ) );
		assertThat( resource.getFileName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetFileNameWithNameOnlyUri() {
		Resource resource = new Resource( URI.create( "file.txt" ) );
		assertThat( resource.getFileName() ).isEqualTo( "file.txt" );
	}

	@Test
	void testGetFileNameWithRootUri() {
		Resource resource = new Resource( URI.create( "/" ) );
		assertThat( resource.getFileName() ).isEqualTo( "/" );
	}

	@Test
	void testGetFileNameWithFolderUri() {
		Resource resource = new Resource( URI.create( "/home/test/" ) );
		assertThat( resource.getFileName() ).isEqualTo( "test" );
	}

	@Test
	void isNew() {
		Resource resource = new Resource( new MockResourceType( getProduct() ), URI.create( "new:" + UUID.randomUUID() ) );
		assertThat( resource.isNew() ).isTrue();
	}

}
