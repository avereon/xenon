package com.parallelsymmetry.essence.settings;

import com.parallelsymmetry.essence.ProgramEventCollector;
import com.parallelsymmetry.essence.UnixPrintWriter;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PersistentSettingsTest {

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private String defaultPath = "target/settings.properties";

	private void removeSettingsFile( File file ) throws IOException {
		if( file.exists() ) FileUtils.forceDelete( file );
		if( file.exists() ) throw new IllegalStateException( "Settings file still exists but should not for test" );
	}

	@Test
	public void testConstructorWithMissingFile() throws IOException {
		File file = new File( defaultPath );
		removeSettingsFile( file );

		// Create the settings object
		PersistentSettings settings = new PersistentSettings( executor, file );

		assertThat( settings.size(), is( 0 ) );
	}

	@Test
	public void testConstructorWithPreexistingFile() throws IOException {
		File file = new File( defaultPath );
		removeSettingsFile( file );

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

	@Test
	public void testPutAndGet() throws Exception {
		File file = new File( defaultPath );
		removeSettingsFile( file );

		PersistentSettings settings = new PersistentSettings( executor, file );

		// Check that the setting is null first
		assertThat( settings.get( "setting/test/a" ), is( nullValue() ) );

		// Set the setting and check the value
		settings.put( "setting/test/a", "a" );
		assertThat( settings.get( "setting/test/a" ), is( "a" ) );

		// Check that the setting is null again
		settings.put( "setting/test/a", null );
		assertThat( settings.get( "setting/test/a" ), is( nullValue() ) );
	}

	@Test
	public void testSize() throws Exception {
		File file = new File( defaultPath );
		removeSettingsFile( file );

		PersistentSettings settings = new PersistentSettings( executor, file );

		int count = 50;
		for( int index = 0; index < count; index++ ) {
			settings.put( "setting/" + index, String.valueOf( index ) );
		}

		assertThat( settings.size(), is( count ) );
	}

	@Test
	public void testPersistence() throws Exception {
		File file = new File( "target/persistence.test.settings.properties" );
		removeSettingsFile( file );

		ProgramEventCollector collector = new ProgramEventCollector();
		PersistentSettings settings = new PersistentSettings( executor, file );
		settings.addEventListener( collector );

		assertThat( collector.getEvents().size(), is( 0 ) );
		settings.put( "test1", "a" );
		assertThat( collector.getEvents().size(), is( 0 ) );
		settings.put( "test1", "b" );
		assertThat( collector.getEvents().size(), is( 0 ) );
		settings.put( "test1", "c" );
		assertThat( collector.getEvents().size(), is( 0 ) );
		settings.put( "test1", "d" );
		assertThat( collector.getEvents().size(), is( 0 ) );
		settings.put( "test1", "e" );
		assertThat( collector.getEvents().size(), is( 0 ) );

		// Wait more than the minimum persist limit
		Thread.sleep( 2 * PersistentSettings.MIN_PERSIST_LIMIT );
		assertThat( collector.getEvents().size(), is( 1 ) );
	}

}
