package com.xeomar.xenon.settings;

import org.junit.Before;

import java.io.File;

public class StoredSettingsTest extends BaseSettingsTest {

	@Before
	public void setup() throws Exception {
		File file = File.createTempFile( "SettingsTest-", "" );
		settings = new StoredSettings( file, null );
		file.deleteOnExit();
	}

}
