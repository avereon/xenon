package com.avereon.xenon.resource;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ResourceTest  {

	@Test
	public void testGetFileNameWithOpaqueUri() {
		Resource resource = new Resource( URI.create( "opaque:name") );
		assertThat( resource.getFileName(), is( "name"));
	}

	@Test
	public void testGetFileNameWithAbsoluteUri() {
		Resource resource = new Resource( URI.create( "file:/path/to/file/file.txt") );
		assertThat( resource.getFileName(), is( "file.txt"));
	}

	@Test
	public void testGetFileNameWithRelativeUri() {
		Resource resource = new Resource( URI.create( "path/to/file/file.txt") );
		assertThat( resource.getFileName(), is( "file.txt"));
	}

	@Test
	public void testGetFileNameWithNameOnlyUri() {
		Resource resource = new Resource( URI.create( "file.txt") );
		assertThat( resource.getFileName(), is( "file.txt"));
	}

}
