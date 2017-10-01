package com.xeomar.xenon.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MapSettings extends AbstractSettings {

	private Map<String, String> values;

	private Settings defaultSettings;

	private String path;

	public MapSettings() {
		this.values = new ConcurrentHashMap<>();
	}

	public MapSettings( Map<String, String> values ) {
		this.values = new ConcurrentHashMap<>( values );
	}

	public MapSettings( Properties properties ) {
		Map<String, String> map = new HashMap<>();
		for( Object key : properties.keySet() ) {
			map.put( key.toString(), properties.getProperty( key.toString() ) );
		}
		this.values = new ConcurrentHashMap<>( map );
	}

	@Override
	public String getPath() {
		return path;
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
	@SuppressWarnings( "unchecked" )
	public String get( String key ) {
		return get( key, null );
	}

	@Override
	@SuppressWarnings( "unchecked" )
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

}
