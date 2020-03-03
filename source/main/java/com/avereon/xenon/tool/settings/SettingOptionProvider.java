package com.avereon.xenon.tool.settings;

import java.util.Set;

public interface SettingOptionProvider {

	Set<String> getKeys();

	String getName( String key );

	String getValue( String key );

}
