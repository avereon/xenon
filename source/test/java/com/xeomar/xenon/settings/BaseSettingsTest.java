package com.xeomar.xenon.settings;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.xeomar.xenon.settings.SettingsMatchers.eventHas;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public abstract class BaseSettingsTest {

	protected Settings settings;

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

		Map<String, String> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );
		MapSettings defaultSettings = new MapSettings( defaultValues );

		// Start by checking the value is null
		assertThat( settings.get( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key ), is( nullValue() ) );

		// Test the default settings
		settings.setDefaultSettings( defaultSettings );
		assertThat( settings.get( key ), is( defaultValue ) );

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key ), is( defaultValue ) );

		settings.setDefaultSettings( null );
		assertThat( settings.get( key ), is( nullValue() ) );
	}

	@Test
	public void testUpdatedEvent() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.addSettingsListener( watcher );

		assertThat( watcher.getEvents().size(), is( 0 ) );

		settings.set( "a", "A" );
		assertThat( watcher.getEvents().get( 0 ), eventHas( settings, SettingsEvent.Type.UPDATED, settings.getPath(), "a", null, "A" ) );

		settings.set( "a", null );
		assertThat( watcher.getEvents().get( 1 ), eventHas( settings, SettingsEvent.Type.UPDATED, settings.getPath(), "a", "A", null ) );
	}

}
