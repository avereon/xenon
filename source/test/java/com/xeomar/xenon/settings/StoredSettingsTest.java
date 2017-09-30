package com.xeomar.xenon.settings;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class StoredSettingsTest {

	private StoredSettings settings;

	@Before
	public void setup() throws Exception {
		File file = File.createTempFile( "SettingsTest-", "" );
		settings = new StoredSettings( file, null );
		file.deleteOnExit();
	}

	@Test
	public void testSetStringAndGetString() {
		String key = "key";
		String value = "value";
		assertThat( settings.get( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key ), is( nullValue() ) );
	}

	@Test
	public void testSetIntegerAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.getInteger( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.getInteger( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.getInteger( key ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetBoolean() {
		String key = "key";
		Boolean value = true;
		assertThat( settings.getBoolean( key ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.getBoolean( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.getBoolean( key ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.getInteger( key ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.getInteger( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.getInteger( key ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetLong() {
		String key = "key";
		Long value = 5L;
		assertThat( settings.getLong( key ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.getLong( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.getLong( key ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetFloat() {
		String key = "key";
		Float value = 5F;
		assertThat( settings.getFloat( key ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.getFloat( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.getFloat( key ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetDouble() {
		String key = "key";
		Double value = 5D;
		assertThat( settings.getDouble( key ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.getDouble( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.getDouble( key ), is( nullValue() ) );
	}

	@Test
	public void testGetValueFromDefaultSettings() {
		String key = "key";
		String value = "value";
		String defaultValue = "defaultValue";

		Map<String,String> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );
		ReadOnlySettings defaultSettings = new ReadOnlySettings( defaultValues );

		// Start by checking the value is null
		assertThat( settings.get( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key ), is( nullValue() ) );

		// Test the default settings
		settings.setDefaultSettings( defaultSettings );
		assertThat( settings.get( key), is( defaultValue ));

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key), is( defaultValue ));

		settings.setDefaultSettings( null );
		assertThat( settings.get( key ), is( nullValue() ) );
	}

}
