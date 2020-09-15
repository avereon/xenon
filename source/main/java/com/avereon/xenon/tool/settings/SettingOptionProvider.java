package com.avereon.xenon.tool.settings;

import java.util.List;
import java.util.Objects;

public interface SettingOptionProvider {

	/**
	 * This key is used when implementations want to include an option that will
	 * return a null value, as is common for default values. The key should be
	 * added to the list of keys returned from {@link #getKeys} in order to be
	 * used. If it is added, then an appropriate name should be returned from
	 * {@link #getName}.
	 */
	String NULL_VALUE_OPTION_KEY = "null";

	List<String> getKeys();

	String getName( String key );

	default String getValue( String key ) {
		return Objects.equals( NULL_VALUE_OPTION_KEY, key ) ? null : key;
	}

}
