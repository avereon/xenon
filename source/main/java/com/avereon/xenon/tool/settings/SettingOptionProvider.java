package com.avereon.xenon.tool.settings;

import java.util.List;

public interface SettingOptionProvider {

	List<String> getKeys();

	String getName( String key );

	String getValue( String key );

}
