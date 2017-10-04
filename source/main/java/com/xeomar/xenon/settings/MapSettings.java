package com.xeomar.xenon.settings;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapSettings extends AbstractSettings {

	// The map of settings. Should only be stored in the root node
	private Map<String, Settings> settingsMap;

	// The settings defaults. Should only be stored in the root node
	private Settings defaultSettings;

	// The settings node values
	private Map<String, String> values;

	private String path;

	public MapSettings() {
		init( "/", new HashMap<>() );
	}

	public MapSettings( String path ) {
		init( path, new HashMap<>() );
	}

	public MapSettings( String path, Map<String, String> map ) {
		init( path, map );
	}

	public MapSettings( String path, Properties properties ) {
		Map<String, String> map = new HashMap<>();
		for( Object key : properties.keySet() ) {
			map.put( key.toString(), properties.getProperty( key.toString() ) );
		}
		init( path, map );
	}

	private void init( String path, Map<String, String> values ) {
		this.settingsMap = new ConcurrentHashMap<>();
		this.values = new ConcurrentHashMap<>( values );
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Settings getSettings( String path ) {
		int index = path.indexOf( "/" );
		String name = index < 0 ? path : path.substring( 0, index );

		Settings child = settingsMap.get( name );
		if( child == null ) {
			child = new MapSettings( this.path + "/" + name );
			settingsMap.put( name, child );
		}

		return index < 0 ? child : child.getSettings( path.substring( index + 1 ) );
	}

	@Override
	public String[] getChildren() {
		List<String> children = new ArrayList<>( settingsMap.keySet() );
		Collections.sort( children );
		return children.toArray( new String[ children.size() ] );
	}

	@Override
	public void set( String key, Object value ) {
		String oldValue = values.get( key );
		String newValue = value == null ? null : String.valueOf( value );
		if( newValue == null ) {
			values.remove( key );
		} else {
			values.put( key, newValue );
		}
		if( !Objects.equals( oldValue, value ) ) fireEvent( new SettingsEvent( this, SettingsEvent.Type.UPDATED, path, key, oldValue, newValue ) );
	}

	@Override
	public String get( String key ) {
		return get( key, null );
	}

	@Override
	public String get( String key, Object defaultValue ) {
		Object object = values.get( key );
		String value = object == null ? null : object.toString();
		if( value == null && defaultSettings != null ) value = defaultSettings.get( key );
		if( value == null ) value = defaultValue == null ? null : defaultValue.toString();
		return value;
	}

	@Override
	public Settings getDefaultSettings() {
		return defaultSettings;
	}

	@Override
	public void setDefaultSettings( Settings settings ) {
		this.defaultSettings = settings;
	}

	@Override
	public void flush() {}

	@Override
	public void delete() {
		// NEXT Delete this settings object in the parent
		// OR Reimplement with a static map of settings and simply remove this from the map
	}

}
