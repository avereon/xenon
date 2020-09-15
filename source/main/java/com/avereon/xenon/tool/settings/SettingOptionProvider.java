package com.avereon.xenon.tool.settings;

import java.util.List;

public interface SettingOptionProvider {

	List<String> getKeys();

	String getName( String key );

	default String getValue( String key ) {
		return key;
	}

}
