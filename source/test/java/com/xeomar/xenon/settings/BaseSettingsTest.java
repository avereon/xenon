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

	@Test
	public void testExists() {
		assertThat( settings.exists( "/" ), is( true ) );
	}

	@Test
	public void testGetPath() {
		assertThat( settings.getPath(), startsWith( "/" ) );
	}

	@Test
	public void testGetNode() {
		Settings peer = settings.getNode( "peer" );
		assertThat( peer, instanceOf( settings.getClass() ) );
		assertThat( peer.getPath(), is( "/peer" ) );

		// Is the settings object viable
		peer.set( "a", "A" );
		peer.flush();
		assertThat( peer.get( "a" ), is( "A" ) );
	}

	@Test
	public void testGetGrandNodes() {
		assertThat( settings.getPath(), startsWith( "" ) );

		Settings childSettings = settings.getNode( "child" );
		Settings grandchildSettings = childSettings.getNode( "grand" );
		assertThat( grandchildSettings, instanceOf( settings.getClass() ) );
		assertThat( grandchildSettings.getPath(), is( "/child/grand" ) );

		// Is the settings object viable
		grandchildSettings.set( "a", "A" );
		grandchildSettings.flush();
		assertThat( grandchildSettings.get( "a" ), is( "A" ) );
	}

	@Test
	public void testGetNodeReturnsSameObject() {
		assertThat( settings.getNode( "" ), is( settings ) );
		assertThat( settings.getNode( "/" ), is( settings ) );
	}

	@Test
	public void testGetNodes() {
		String folder = "children/" + String.format( "%08x", new Random().nextInt() );
		Settings childA = settings.getNode( folder + "/a" );
		Settings childB = settings.getNode( folder + "/b" );
		Settings childC = settings.getNode( folder + "/c" );

		childA.set( "a", "A" );
		childB.set( "b", "B" );
		childC.set( "c", "C" );

		childA.flush();
		childB.flush();
		childC.flush();

		Settings children = settings.getNode( folder );
		assertThat( children.getNodes().length, is( 3 ) );
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

		Map<String, String> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );

		// Start by checking the value is null
		assertThat( settings.get( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key ), is( nullValue() ) );

		// Test the default settings
		settings.setDefaultValues( defaultValues );
		assertThat( settings.get( key ), is( defaultValue ) );

		settings.set( key, value );
		assertThat( settings.get( key ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key ), is( defaultValue ) );

		settings.setDefaultValues( null );
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
	public void testDelete() {
		assertThat( settings.exists( "test" ), is( false ) );
		Settings test = settings.getNode( "test" );
		test.flush();
		assertThat( settings.exists( "test" ), is( true ) );
		test.delete();
		assertThat( settings.exists( "test" ), is( false ) );
	}

}
