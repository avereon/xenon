package com.xeomar.xenon.settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ReadOnlySettings implements Settings {

	private Map<String, String> values;

	private Settings defaultSettings;

	public ReadOnlySettings( Properties properties ) {
		Map<String, String> map = new HashMap<>();
		for( Object key : properties.keySet() ) {
			map.put( key.toString(), properties.getProperty( key.toString() ) );
		}
		this.values = Collections.unmodifiableMap( map );
	}

	public ReadOnlySettings( Map<String, String> values ) {
		this.values = Collections.unmodifiableMap( values );
	}

	@Override
	public void set( String key, Object value ) {
		values.put( key, value.toString() );
	}

	@Override
	public Boolean getBoolean( String key ) {
		return Boolean.parseBoolean( get( key ) );
	}

	@Override
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		return Boolean.parseBoolean( get( key, defaultValue.toString() ) );
	}

	@Override
	public Integer getInteger( String key ) {
		return Integer.parseInt( get( key ) );
	}

	@Override
	public Integer getInteger( String key, Integer defaultValue ) {
		return Integer.parseInt( get( key, defaultValue.toString() ) );
	}

	@Override
	public Long getLong( String key ) {
		return Long.parseLong( get( key ) );
	}

	@Override
	public Long getLong( String key, Long defaultValue ) {
		return Long.parseLong( get( key, defaultValue.toString() ) );
	}

	@Override
	public Float getFloat( String key ) {
		return Float.parseFloat( get( key ) );
	}

	@Override
	public Float getFloat( String key, Float defaultValue ) {
		return Float.parseFloat( get( key, defaultValue.toString() ) );
	}

	@Override
	public Double getDouble( String key ) {
		return Double.parseDouble( get( key ) );
	}

	@Override
	public Double getDouble( String key, Double defaultValue ) {
		return Double.parseDouble( get( key, defaultValue.toString() ) );
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
		if( value == null ) value = defaultValue == null? null : defaultValue.toString();
		return value;
	}

	@Override
	public Settings getDefaultSettings() {
		return null;
	}

	@Override
	public void setDefaultSettings( Settings settings ) {

	}

	@Override
	public void addSettingsListener( SettingsListener listener ) {}

	@Override
	public void removeSettingsListener( SettingsListener listener ) {}

	@Override
	public void flush() {}

}
