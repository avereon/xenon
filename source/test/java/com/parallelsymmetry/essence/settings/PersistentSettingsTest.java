package com.parallelsymmetry.essence.settings;

import com.parallelsymmetry.essence.UnixPrintWriter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PersistentSettingsTest {

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Test
	public void testConstructorWithFile() throws IOException {
		File file = new File( "target/settings.properties" );

		// Ensure the file does not exist
		FileUtils.forceDelete( file );
		if( file.exists() ) throw new IllegalStateException( "Settings file still exists but should not for test" );

		// Create the settings object
		PersistentSettings settings = new PersistentSettings( executor, file );

		assertThat( settings.size(), is( 0 ) );
	}

	@Test
	public void testConstructorWithPreexistingFile() throws IOException {
		File file = new File( "target/settings.properties" );

		// Create the settings file
		PrintWriter writer = new UnixPrintWriter( new FileWriter( file ) );
		writer.println( "path/to/setting=42" );
		writer.close();

		// Verify the file exists
		if( !file.exists() ) throw new IllegalStateException( "Settings file does not exist but should not for test" );

		PersistentSettings settings = new PersistentSettings( executor, file );

		assertThat( settings.size(), is( 1 ) );
		assertThat( settings.get( "path/to/setting" ), is( "42" ) );
	}

}
