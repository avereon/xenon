package com.xeomar.xenon.settings;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingsFactoryTest {

	@Test
	public void testRoot() {
		File root = new File( "test/path" );
		SettingsFactory.setRoot( root );
		assertThat( SettingsFactory.getRoot(), is( root) );
	}

	@Test
	public void testGetSettings() {
		Settings settings  = SettingsFactory.getSettings( "/program" );
		//assertThat( settings.getPath(), is( "/program" ) );
	}

}
