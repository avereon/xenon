package com.xeomar.xenon.settings;

public interface SettingMap {

	<T> T get(String key );

	<T> T get(String key, T defaultValue );

	<T> void put(String key, T value );

}
