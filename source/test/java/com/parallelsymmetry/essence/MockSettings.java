package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.settings.Settings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockSettings implements Settings {

	private Map<String, Object> values = new ConcurrentHashMap<>();

	@Override
	public Boolean getBoolean( String key ) {
		return Boolean.parseBoolean( getString( key ) );
	}

	@Override
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		return Boolean.parseBoolean( getString( key, defaultValue.toString() ) );
	}

	@Override
	public Integer getInteger( String key ) {
		return Integer.parseInt( getString( key ) );
	}

	@Override
	public Integer getInteger( String key, Integer defaultValue ) {
		return Integer.parseInt( getString( key, defaultValue.toString() ) );
	}

	@Override
	public Long getLong( String key ) {
		return Long.parseLong( getString( key ) );
	}

	@Override
	public Long getLong( String key, Long defaultValue ) {
		return Long.parseLong( getString( key, defaultValue.toString() ));
	}

	@Override
	public Float getFloat( String key ) {
		return Float.parseFloat( getString( key ) );
	}

	@Override
	public Float getFloat( String key, Float defaultValue ) {
		return Float.parseFloat( getString( key, defaultValue.toString() ));
	}

	@Override
	public Double getDouble( String key ) {
		return Double.parseDouble( getString( key ) );
	}

	@Override
	public Double getDouble( String key, Double defaultValue ) {
		return Double.parseDouble( getString( key, defaultValue.toString() ));
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public String getString( String key ) {
		Object value = values.get( key );
		return value == null ? null : value.toString();
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public String getString( String key, String defaultValue ) {
		String value = getString( key );
		if( value == null ) value = defaultValue;
		return value;
	}

	@Override
	public void set( String key, Object value ) {
		if( value == null ) {
			values.remove( key );
		} else {
			values.put( key, value.toString() );
		}
	}

}
