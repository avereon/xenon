package com.xeomar.xenon.settings;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.nio.file.Files;

public class StoredSettingsTest extends BaseSettingsTest {

	private File file;

	@Before
	public void setup() throws Exception {
		file = Files.createTempDirectory( SETTINGS_NAME ).toFile();
		settings = new StoredSettings( file );
	}

	@After
	public void cleanup() throws Exception {
		FileUtils.forceDeleteOnExit( file );
	}

}
