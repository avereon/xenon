package com.xeomar.xenon.settings;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.xeomar.xenon.settings.SettingsMatchers.eventHas;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class BaseSettingsTest {

	protected static final String SETTINGS_NAME = "SettingsTest";

	protected Settings settings;

//	@Test
//	public void testGetSettings() {
//		Settings root = settings;
//		assertThat( root.getSettings("/"), is( root ) );
//		assertThat( root.getSettings(""), is( root ) );
//
//	}

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
		MapSettings defaultSettings = new MapSettings( "default", defaultValues );

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

	@Test
	public void testChildSettings() {
		assertThat( settings.getPath(), startsWith( "" ) );

		Settings peerSettings = settings.getSettings( "peer" );
		assertThat( peerSettings, instanceOf( settings.getClass() ) );
		assertThat( peerSettings.getPath(), is( "/peer" ) );

		// Is the settings object viable
		peerSettings.set( "a", "A" );
		peerSettings.flush();
		assertThat( peerSettings.get( "a" ), is( "A" ) );
	}

	@Test
	public void testGrandchildSettings() {
		assertThat( settings.getPath(), startsWith( "" ) );

		Settings childSettings = settings.getSettings( "child" );
		Settings grandchildSettings = childSettings.getSettings( "grand" );
		assertThat( grandchildSettings, instanceOf( settings.getClass() ) );
		assertThat( grandchildSettings.getPath(), is( "/child/grand" ) );

		// Is the settings object viable
		grandchildSettings.set( "a", "A" );
		grandchildSettings.flush();
		assertThat( grandchildSettings.get( "a" ), is( "A" ) );
	}

	@Test
	public void testGetChildren() {
		String folder = "children/" + String.format( "%08x", new Random().nextInt() );
		Settings childA = settings.getSettings( folder + "/a" );
		Settings childB = settings.getSettings( folder + "/b" );
		Settings childC = settings.getSettings( folder + "/c" );

		childA.set( "a", "A" );
		childB.set( "b", "B" );
		childC.set( "c", "C" );

		childA.flush();
		childB.flush();
		childC.flush();

		Settings children = settings.getSettings( folder );
		assertThat( children.getChildren().length, is( 3 ) );
	}

}
