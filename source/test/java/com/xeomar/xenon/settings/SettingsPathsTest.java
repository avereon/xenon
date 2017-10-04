package com.xeomar.xenon.settings;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
		assertThat( SettingsPaths.isRelative( "/test/path" ), is( false ) );
		assertThat( SettingsPaths.isRelative( "/test/path/" ), is( false ) );
	}

	@Test
	public void testGetParent() {
		assertThat( SettingsPaths.getParent( null ), is( nullValue() ) );
		assertThat( SettingsPaths.getParent( "" ), is( nullValue() ) );
		assertThat( SettingsPaths.getParent( "test" ), is( "" ) );
		assertThat( SettingsPaths.getParent( "test/path" ), is( "test" ) );
		assertThat( SettingsPaths.getParent( "test/path/" ), is( "test" ) );
	}

	@Test
	public void testResolve() {
		// Normal paths
		assertThat( SettingsPaths.resolve( null ), is( nullValue() ) );
		assertThat( SettingsPaths.resolve( "" ), is( "" ) );
		assertThat( SettingsPaths.resolve( "/" ), is( "/" ) );
		assertThat( SettingsPaths.resolve( "/test" ), is( "/test" ) );
		assertThat( SettingsPaths.resolve( "/test/path" ), is( "/test/path" ) );

		// Trailing separators
		assertThat( SettingsPaths.resolve( "/test/" ), is( "/test" ) );
		assertThat( SettingsPaths.resolve( "/test/path/" ), is( "/test/path" ) );

		// Multiple separators
		assertThat( SettingsPaths.resolve( "/////test" ), is( "/test" ) );
		assertThat( SettingsPaths.resolve( "/test/////path" ), is( "/test/path" ) );

		// Parent references
		assertThat( SettingsPaths.resolve( "/../test" ), is( "" ) );
		assertThat( SettingsPaths.resolve( "/test/../path" ), is( "/path" ) );
	}
}
