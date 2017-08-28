package com.xeomar.xenon.settings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Config implements SettingMap {

	private Map<String,Object> settings;

	public Config() {
		settings = new ConcurrentHashMap<>();
	}

	@Override
	public <T> T get( String key ) {
		return (T)settings.get(key);
	}

	@Override
	public <T> T get( String key, T defaultValue ) {
		T value = (T)settings.get(key);
		return value != null ? value : defaultValue ;
	}

	@Override
	public <T> void put( String key, T value ) {
		settings.put(key, value );
	}

}
