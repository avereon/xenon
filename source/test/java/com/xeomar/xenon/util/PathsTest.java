package com.xeomar.xenon.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class PathsTest {

	@Test
	public void testIsAbsolute() {
		assertThat( Paths.isAbsolute( "/" ), is( true ) );
		assertThat( Paths.isAbsolute( "/test" ), is( true ) );
		assertThat( Paths.isAbsolute( "/test/path" ), is( true ) );
		assertThat( Paths.isAbsolute( "/test/path/" ), is( true ) );

		assertThat( Paths.isAbsolute( null ), is( false ) );
		assertThat( Paths.isAbsolute( "" ), is( false ) );
		assertThat( Paths.isAbsolute( "test" ), is( false ) );
		assertThat( Paths.isAbsolute( "test/path" ), is( false ) );
	}

	@Test
	public void testIsRelative() {
		assertThat( Paths.isRelative( null ), is( false ) );
		assertThat( Paths.isRelative( "" ), is( true ) );
		assertThat( Paths.isRelative( "test" ), is( true ) );
		assertThat( Paths.isRelative( "test/path" ), is( true ) );

		assertThat( Paths.isRelative( "/" ), is( false ) );
		assertThat( Paths.isRelative( "/test" ), is( false ) );
		assertThat( Paths.isRelative( "/test/" ), is( false ) );
		assertThat( Paths.isRelative( "/test/path" ), is( false ) );
		assertThat( Paths.isRelative( "/test/path/" ), is( false ) );
	}

	@Test
	public void testGetParent() {
		assertThat( Paths.getParent( null ), is( nullValue() ) );
		assertThat( Paths.getParent( "" ), is( nullValue() ) );
		assertThat( Paths.getParent( "/" ), is( nullValue() ) );

		assertThat( Paths.getParent( "test" ), is( "" ) );
		assertThat( Paths.getParent( "test/path" ), is( "test" ) );
		assertThat( Paths.getParent( "test/path/" ), is( "test" ) );

		assertThat( Paths.getParent( "/test" ), is( "/" ) );
		assertThat( Paths.getParent( "/test/" ), is( "/" ) );
		assertThat( Paths.getParent( "/test/path" ), is( "/test" ) );
		assertThat( Paths.getParent( "/test/path/" ), is( "/test" ) );

		assertThat( Paths.getParent( "/../test" ), is( "/.." ) );
	}

	@Test
	public void testNormalize() {
		// Normal paths
		assertThat( Paths.normalize( null ), is( nullValue() ) );
		assertThat( Paths.normalize( "" ), is( "" ) );
		assertThat( Paths.normalize( "/" ), is( "/" ) );
		assertThat( Paths.normalize( "/test" ), is( "/test" ) );
		assertThat( Paths.normalize( "/test/path" ), is( "/test/path" ) );

		// Trailing separators
		assertThat( Paths.normalize( "/test/" ), is( "/test" ) );
		assertThat( Paths.normalize( "/test/path/" ), is( "/test/path" ) );

		// Multiple separators
		assertThat( Paths.normalize( "/////test" ), is( "/test" ) );
		assertThat( Paths.normalize( "/test/////path" ), is( "/test/path" ) );

		// Parent references
		assertThat( Paths.normalize( "/.." ), is( nullValue() ) );
		assertThat( Paths.normalize( "/../test" ), is( nullValue() ) );
		assertThat( Paths.normalize( "/test/../path" ), is( "/path" ) );
	}

	@Test
	public void testResolve() {
		// Null paths
		assertThat( Paths.resolve( null, "" ), is( nullValue() ) );
		assertThat( Paths.resolve( "", null ), is( nullValue() ) );

		// Empty paths
		assertThat( Paths.resolve( "", "" ), is( "" ) );
		assertThat( Paths.resolve( "/", "" ), is( "/" ) );
		assertThat( Paths.resolve( "", "/" ), is( "/" ) );
		assertThat( Paths.resolve( "foo/bar", "" ), is( "foo/bar" ) );
		assertThat( Paths.resolve( "", "foo/bar" ), is( "foo/bar" ) );

		// Normal paths
		assertThat( Paths.resolve( "/", "foo" ), is( "/foo" ) );
		assertThat( Paths.resolve( "/foo", "bar" ), is( "/foo/bar" ) );
		assertThat( Paths.resolve( "/foo", "/bar" ), is( "/bar" ) );
		assertThat( Paths.resolve( "/foo/bar", "gus" ), is( "/foo/bar/gus" ) );
		assertThat( Paths.resolve( "/foo", "bar/gus" ), is( "/foo/bar/gus" ) );
		assertThat( Paths.resolve( "foo", "bar" ), is( "foo/bar" ) );
		assertThat( Paths.resolve( "foo", "/bar" ), is( "/bar" ) );
		assertThat( Paths.resolve( "foo/bar", "gus" ), is( "foo/bar/gus" ) );
		assertThat( Paths.resolve( "foo", "bar/gus" ), is( "foo/bar/gus" ) );
	}

	@Test
	public void testRelativize() {
		// Null paths
		assertThat( Paths.relativize( null, "" ), is( nullValue() ) );
		assertThat( Paths.relativize( null, "/" ), is( nullValue() ) );
		assertThat( Paths.relativize( null, "/foo" ), is( nullValue() ) );
		assertThat( Paths.relativize( "", null ), is( nullValue() ) );
		assertThat( Paths.relativize( "/", null ), is( nullValue() ) );
		assertThat( Paths.relativize( "/foo", null ), is( nullValue() ) );

		// Equals paths
		assertThat( Paths.relativize( "/", "/" ), is( "" ) );
		assertThat( Paths.relativize( "/foo", "/foo" ), is( "" ) );
		assertThat( Paths.relativize( "/foo/bar", "/foo/bar" ), is( "" ) );
		assertThat( Paths.relativize( "/foo/bar/gus", "/foo/bar/gus" ), is( "" ) );

		// Empty source
		assertThat( Paths.relativize( "", "" ), is( "" ) );
		assertThat( Paths.relativize( "", "foo" ), is( "foo" ) );
		assertThat( Paths.relativize( "", "foo/bar" ), is( "foo/bar" ) );

		// Root sources
		assertThat( Paths.relativize( "/", "/foo" ), is( "foo" ) );
		assertThat( Paths.relativize( "/", "/foo/bar" ), is( "foo/bar" ) );
		assertThat( Paths.relativize( "/", "/foo/bar/gus" ), is( "foo/bar/gus" ) );

		// Small sources
		assertThat( Paths.relativize( "/foo", "/foo/bar" ), is( "bar" ) );
		assertThat( Paths.relativize( "/foo", "/foo/bar/gus" ), is( "bar/gus" ) );

		// Root targets
		assertThat( Paths.relativize( "/foo", "/" ), is( ".." ) );
		assertThat( Paths.relativize( "/foo/bar", "/" ), is( "../.." ) );
		assertThat( Paths.relativize( "/foo/bar/gus", "/" ), is( "../../.." ) );

		// Small targets
		assertThat( Paths.relativize( "/foo/bar", "/foo" ), is( ".." ) );
		assertThat( Paths.relativize( "/foo/bar/gus", "/foo" ), is( "../.." ) );

		// Mismatch paths
		assertThat( Paths.relativize( "/foo/bar/gus", "/foo/bar/sag" ), is( "../sag" ) );
		assertThat( Paths.relativize( "/foo/bar/gus", "/foo/gin/sag" ), is( "../../gin/sag" ) );
		assertThat( Paths.relativize( "/foo/bar/gus", "/foo/gus/bar" ), is( "../../gus/bar" ) );
		assertThat( Paths.relativize( "/foo/bar/gus", "/gus/bar/foo" ), is( "../../../gus/bar/foo" ) );
	}

	@Test
	public void testRelativizeWithMixedAbsoluteAndRelativePaths() {
		// Absolute and relative
		try {
			Paths.relativize( "/foo", "bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage(), is( "Target is different type of path" ) );
		}

		// Relative and absolute
		try {
			Paths.relativize( "foo", "/bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage(), is( "Target is different type of path" ) );
		}
	}

	@Test
	public void testParseNames() {
		assertThat( Paths.parseNames( "/foo/bar" ), is( new String[]{ "/", "foo", "bar" } ) );
		assertThat( Paths.parseNames( "/foo/bar/" ), is( new String[]{ "/", "foo", "bar" } ) );
		assertThat( Paths.parseNames( "foo/bar" ), is( new String[]{ "foo", "bar" } ) );
		assertThat( Paths.parseNames( "/" ), is( new String[]{ "/" } ) );
		assertThat( Paths.parseNames( " " ), is( new String[]{ " " } ) );
		assertThat( Paths.parseNames( "" ), is( new String[]{ "" } ) );
		assertThat( Paths.parseNames( null ), is( nullValue() ) );
	}

}
