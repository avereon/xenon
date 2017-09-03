package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.node.Node;

public class SettingOption extends Node {

	static final String KEY = "key";

	static final String NAME = "name";

	static final String VALUE = "value";

	public SettingOption( String key, String name, String value ) {
		if( key == null || name == null || value == null ) throw new IllegalArgumentException( "Key cannot be null: " + key + ", " + name + ", " + value );

		super.setValue( KEY, key );
		super.setValue( NAME, name );
		super.setValue( VALUE, value );
		setModified( false );

		definePrimaryKey( KEY, NAME, VALUE );
		defineBusinessKey( KEY, NAME, VALUE );
		defineReadOnly( KEY, NAME, VALUE );
	}

	public String getKey() {
		return getValue( KEY );
	}

	public String getName() {
		return getValue( NAME );
	}

	public String getOptionValue() {
		return getValue( VALUE );
	}

	@Override
	public String toString() {
		return getName();
	}

}
