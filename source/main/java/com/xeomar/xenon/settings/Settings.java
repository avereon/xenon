package com.xeomar.xenon.settings;

import java.util.Map;

public interface Settings {

	String getPath();

	String[] getChildren();

	/**
	 * Get a settings object for the specified path. If the path starts with the slash character then the path is absolute. If the path does not start with the slash character then the path is relative to this settings node.
	 * <p>
	 * Multiple requests from the same settings tree using the same path should return the same settings object.
	 *
	 * @param path The requested path
	 * @return The settings object for the path
	 */
	Settings getChild( String path );

	Settings getChild( String path, Map<String, String> values );

	void set( String key, Object value );

	String get( String key );

	String get( String key, Object defaultValue );

	Boolean getBoolean( String key );

	Boolean getBoolean( String key, Boolean defaultValue );

	Integer getInteger( String key );

	Integer getInteger( String key, Integer defaultValue );

	Long getLong( String key );

	Long getLong( String key, Long defaultValue );

	Float getFloat( String key );

	Float getFloat( String key, Float defaultValue );

	Double getDouble( String key );

	Double getDouble( String key, Double defaultValue );

	Map<String, String> getDefaultValues();

	void setDefaultValues( Map<String, String> defaults );

	void addSettingsListener( SettingsListener listener );

	void removeSettingsListener( SettingsListener listener );

	void flush();

	void delete();

}
