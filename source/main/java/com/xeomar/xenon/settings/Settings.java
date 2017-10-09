package com.xeomar.xenon.settings;

import java.util.Map;

public interface Settings {

	/**
	 * Get the absolute path of the settings node.
	 *
	 * @return The absolute path of the settings node
	 */
	String getPath();

	/**
	 * Check if the node at the specified path exists.
	 *
	 * @param path The node path to check
	 * @return True if the node exists, false otherwise
	 */
	boolean exists( String path );

	/**
	 * Get a settings object for the specified path. If the path starts with the separator character then the path is absolute. If the path does not start with the separator character then the path is relative to this settings node.
	 * <p>
	 * Multiple requests from the same settings tree using the same path return the same settings object.
	 *
	 * @param path The requested path
	 * @return The settings object for the path
	 */
	Settings getNode( String path );

	/**
	 * Get a settings object for the specified path according to {@link #getNode(String)} with the specified values.
	 *
	 * @param path The requested path
	 * @param values The initial values of the settings
	 * @return The settings object for the path
	 */
	Settings getNode( String path, Map<String, String> values );

	/**
	 * Get the existing child node names of this settings node.
	 *
	 * @return The names of the existing child nodes
	 */
	String[] getNodes();

	/**
	 * Set a value in the settings object.
	 *  @param key The value key
	 * @param value The value
	 */
	void set( String key, Object value );

	/**
	 * Get a value from the settings object.
	 *
	 * @param key The value key
	 * @return The value as a string
	 */
	String get( String key );

	/**
	 * Get a value from the settings object.
	 *
	 * @param key The value key
	 * @param defaultValue The default value
	 * @return The value as a string or the default value if the value does not already exist
	 */
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

	/**
	 * Get the default values for this settings node.
	 *
	 * @return The default values map
	 */
	Map<String, String> getDefaultValues();

	/**
	 * Set the default values for this settings node.
	 *
	 * @param defaults The default values map
	 */
	void setDefaultValues( Map<String, String> defaults );

	/**
	 * Add a settings listener to this node. The settings listener will not receive event from child nodes.
	 *
	 * @param listener The settings listener
	 */
	void addSettingsListener( SettingsListener listener );

	/**
	 * Remove a settings listener from this node.
	 *
	 * @param listener The settings listener
	 */
	void removeSettingsListener( SettingsListener listener );

	/**
	 * Flush the settings values. For settings implementations that store values this method should be used to store the values promptly.
	 */
	void flush();

	/**
	 * Delete this settings node.
	 */
	void delete();

}
