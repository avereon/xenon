package com.xeomar.xenon.settings;

public interface Settings {

	String getPath();

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

}
