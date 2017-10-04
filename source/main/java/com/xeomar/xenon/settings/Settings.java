package com.xeomar.xenon.settings;

public interface Settings {

	/**
	 * Get a settings object for the specified path. If the path starts with the
	 * slash character then the path is absolute. If the path does not start with
	 * the slash character then the path is relative to this settings node.
	 *
	 * Multiple requests from the same settings tree using the same resovled path
	 * should return the same settings object.
	 *
	 * @param path The requested path
	 * @return The settings object for the path
	 */
	Settings getSettings( String path );

	String getPath();

	String[] getChildren();

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

	Settings getDefaultSettings();

	void setDefaultSettings( Settings settings );

	void addSettingsListener( SettingsListener listener );

	void removeSettingsListener( SettingsListener listener );

	void flush();

	void delete();

}
