package com.xeomar.xenon.settings;

public interface Settings {

	void set( String key, Object value );

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

	String getString( String key );

	String getString( String key, String defaultValue );

	void addSettingsListener( SettingsListener listener );

	void removeSettingsListener( SettingsListener listener );

}
