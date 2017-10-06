package com.xeomar.xenon.settings;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class SettingsPathsTest {

	@Test
	public void testIsAbsolute() {
		assertThat( SettingsPaths.isAbsolute( "/" ), is( true ) );
		assertThat( SettingsPaths.isAbsolute( "/test" ), is( true ) );
		assertThat( SettingsPaths.isAbsolute( "/test/path" ), is( true ) );
		assertThat( SettingsPaths.isAbsolute( "/test/path/" ), is( true ) );

		assertThat( SettingsPaths.isAbsolute( null ), is( false ) );
		assertThat( SettingsPaths.isAbsolute( "" ), is( false ) );
		assertThat( SettingsPaths.isAbsolute( "test" ), is( false ) );
		assertThat( SettingsPaths.isAbsolute( "test/path" ), is( false ) );
	}

	@Test
	public void testIsRelative() {
		assertThat( SettingsPaths.isRelative( null ), is( false ) );
		assertThat( SettingsPaths.isRelative( "" ), is( true ) );
		assertThat( SettingsPaths.isRelative( "test" ), is( true ) );
		assertThat( SettingsPaths.isRelative( "test/path" ), is( true ) );

		assertThat( SettingsPaths.isRelative( "/" ), is( false ) );
		assertThat( SettingsPaths.isRelative( "/test" ), is( false ) );
		assertThat( SettingsPaths.isRelative( "/test/" ), is( false ) );
		assertThat( SettingsPaths.isRelative( "/test/path" ), is( false ) );
		assertThat( SettingsPaths.isRelative( "/test/path/" ), is( false ) );
	}

	@Test
	public void testGetParent() {
		assertThat( SettingsPaths.getParent( null ), is( nullValue() ) );
		assertThat( SettingsPaths.getParent( "" ), is( nullValue() ) );
		assertThat( SettingsPaths.getParent( "/" ), is( nullValue() ) );

		assertThat( SettingsPaths.getParent( "test" ), is( "" ) );
		assertThat( SettingsPaths.getParent( "test/path" ), is( "test" ) );
		assertThat( SettingsPaths.getParent( "test/path/" ), is( "test" ) );

		assertThat( SettingsPaths.getParent( "/test" ), is( "/" ) );
		assertThat( SettingsPaths.getParent( "/test/" ), is( "/" ) );
		assertThat( SettingsPaths.getParent( "/test/path" ), is( "/test" ) );
		assertThat( SettingsPaths.getParent( "/test/path/" ), is( "/test" ) );

		assertThat( SettingsPaths.getParent( "/../test" ), is( "/.." ) );
	}

	@Test
	public void testNormalize() {
		// Normal paths
		assertThat( SettingsPaths.normalize( null ), is( nullValue() ) );
		assertThat( SettingsPaths.normalize( "" ), is( "" ) );
		assertThat( SettingsPaths.normalize( "/" ), is( "/" ) );
		assertThat( SettingsPaths.normalize( "/test" ), is( "/test" ) );
		assertThat( SettingsPaths.normalize( "/test/path" ), is( "/test/path" ) );

		// Trailing separators
		assertThat( SettingsPaths.normalize( "/test/" ), is( "/test" ) );
		assertThat( SettingsPaths.normalize( "/test/path/" ), is( "/test/path" ) );

		// Multiple separators
		assertThat( SettingsPaths.normalize( "/////test" ), is( "/test" ) );
		assertThat( SettingsPaths.normalize( "/test/////path" ), is( "/test/path" ) );

		// Parent references
		assertThat( SettingsPaths.normalize( "/.." ), is( nullValue() ) );
		assertThat( SettingsPaths.normalize( "/../test" ), is( nullValue() ) );
		assertThat( SettingsPaths.normalize( "/test/../path" ), is( "/path" ) );
	}

	@Test
	public void testResolve() {
		// Null paths
		assertThat( SettingsPaths.resolve( null, "" ), is( nullValue() ) );
		assertThat( SettingsPaths.resolve( "", null ), is( nullValue() ) );

		// Empty paths
		assertThat( SettingsPaths.resolve( "", "" ), is( "" ) );
		assertThat( SettingsPaths.resolve( "/", "" ), is( "/" ) );
		assertThat( SettingsPaths.resolve( "", "/" ), is( "/" ) );
		assertThat( SettingsPaths.resolve( "foo/bar", "" ), is( "foo/bar" ) );
		assertThat( SettingsPaths.resolve( "", "foo/bar" ), is( "foo/bar" ) );

		// Normal paths
		assertThat( SettingsPaths.resolve( "/", "foo" ), is( "/foo" ) );
		assertThat( SettingsPaths.resolve( "/foo", "bar" ), is( "/foo/bar" ) );
		assertThat( SettingsPaths.resolve( "/foo", "/bar" ), is( "/bar" ) );
		assertThat( SettingsPaths.resolve( "/foo/bar", "gus" ), is( "/foo/bar/gus" ) );
		assertThat( SettingsPaths.resolve( "/foo", "bar/gus" ), is( "/foo/bar/gus" ) );
		assertThat( SettingsPaths.resolve( "foo", "bar" ), is( "foo/bar" ) );
		assertThat( SettingsPaths.resolve( "foo", "/bar" ), is( "/bar" ) );
		assertThat( SettingsPaths.resolve( "foo/bar", "gus" ), is( "foo/bar/gus" ) );
		assertThat( SettingsPaths.resolve( "foo", "bar/gus" ), is( "foo/bar/gus" ) );
	}

	@Test
	public void testRelativize() {
		assertThat( SettingsPaths.relativize( null, "" ), is( nullValue() ) );
		assertThat( SettingsPaths.relativize( "", null ), is( nullValue() ) );

		// Equals source and target
		assertThat( SettingsPaths.relativize( "/foo", "/foo" ), is( "" ) );
		assertThat( SettingsPaths.relativize( "/foo/bar", "/foo/bar" ), is( "" ) );

		// Empty source
		assertThat( SettingsPaths.relativize( "", "" ), is( "" ) );
		assertThat( SettingsPaths.relativize( "", "foo" ), is( "foo" ) );
		assertThat( SettingsPaths.relativize( "", "foo/bar" ), is( "foo/bar" ) );

		// NEXT Continue to test relativize
	}

	@Test
	public void testRelativizeWithMixedAbsoluteAndRelativePaths() {
		// Absolute and relative
		try {
		SettingsPaths.relativize( "/foo", "bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage(), is( "Target is different type of path" ) );
		}

		// Relative and absolute
		try {
			SettingsPaths.relativize( "foo", "/bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage(), is( "Target is different type of path" ) );
		}
	}

}
