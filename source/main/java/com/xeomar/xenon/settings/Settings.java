package com.xeomar.xenon.settings;

public interface Settings {

	<T> T get(String key );

	<T> T get(String key, T defaultValue );

	<T> void set( String key, T value );

	@Deprecated
	Boolean getBoolean( String key );

	@Deprecated
	Boolean getBoolean( String key, Boolean defaultValue );

	@Deprecated
	Integer getInteger( String key );

	@Deprecated
	Integer getInteger( String key, Integer defaultValue );

	@Deprecated
	Long getLong( String key );

	@Deprecated
	Long getLong( String key, Long defaultValue );

	@Deprecated
	Float getFloat( String key );

	@Deprecated
	Float getFloat( String key, Float defaultValue );

	@Deprecated
	Double getDouble( String key );

	@Deprecated
	Double getDouble( String key, Double defaultValue );

	@Deprecated
	String getString( String key );

	@Deprecated
	String getString( String key, String defaultValue );

}
