package com.xeomar.xenon.settings;

import com.xeomar.xenon.util.Paths;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractSettings implements Settings {

	private Set<SettingsListener> listeners;

	protected AbstractSettings() {
		this.listeners = new CopyOnWriteArraySet<>();
	}

	@Override
	@Deprecated
	public Boolean getBoolean( String key ) {
		return getBoolean( key, null );
	}

	@Override
	@Deprecated
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Boolean.parseBoolean( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Integer getInteger( String key ) {
		return getInteger( key, null );
	}

	@Override
	@Deprecated
	public Integer getInteger( String key, Integer defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Integer.parseInt( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Long getLong( String key ) {
		return getLong( key, null );
	}

	@Override
	@Deprecated
	public Long getLong( String key, Long defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Long.parseLong( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Float getFloat( String key ) {
		return getFloat( key, null );
	}

	@Override
	@Deprecated
	public Float getFloat( String key, Float defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Float.parseFloat( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	@Deprecated
	public Double getDouble( String key ) {
		return getDouble( key, null );
	}

	@Override
	@Deprecated
	public Double getDouble( String key, Double defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Double.parseDouble( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	public void addSettingsListener( SettingsListener listener ) {
		listeners.add( listener );
	}

	@Override
	public void removeSettingsListener( SettingsListener listener ) {
		listeners.remove( listener );
	}

	protected void fireEvent( SettingsEvent event ) {
		for( SettingsListener listener : new HashSet<>( listeners ) ) {
			listener.settingsEvent( event );
		}
	}

	protected String getNodePath( String root, String path ) {
		// Resolve the path
		String nodePath = Paths.isAbsolute( path ) ? path : Paths.resolve( root, path );

		// Normalize the path
		return Paths.normalize( nodePath );
	}

}
