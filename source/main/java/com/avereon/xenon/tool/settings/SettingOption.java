package com.avereon.xenon.tool.settings;

import com.avereon.data.Node;

public class SettingOption extends Node {

	private static final String KEY = "key";

	private static final String NAME = "name";

	private static final String VALUE = "value";

	public SettingOption() {
		definePrimaryKey( KEY );
		defineNaturalKey( NAME );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public SettingOption setKey( String key ) {
		setValue( KEY, key );
		return this;
	}

	public String getName() {
		return getValue( NAME );
	}

	public SettingOption setName( String name ) {
		setValue( NAME, name );
		return this;
	}

	public String getOptionValue() {
		return getValue( VALUE );
	}

	public SettingOption setOptionValue( String value ) {
		setValue( VALUE, value );
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}

}
