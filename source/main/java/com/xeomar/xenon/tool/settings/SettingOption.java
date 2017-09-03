package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.node.Node;

public class SettingOption extends Node {

	private static final String KEY = "key";

	private static final String NAME = "name";

	private static final String VALUE = "value";

	public SettingOption() {
		definePrimaryKey( KEY );
		defineBusinessKey( NAME );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public void setKey( String key ) {
		setValue( KEY, key );
	}

	public String getName() {
		return getValue( NAME );
	}

	public void setName( String name ) {
		setValue( NAME, name );
	}

	public String getOptionValue() {
		return getValue( VALUE );
	}

	public void setOptionValue( String value ) {
		setValue( VALUE, value );
	}

	@Override
	public String toString() {
		return getName();
	}

}
