package com.xeomar.xenon.settings;

import java.util.EventObject;

public class SettingsEvent extends EventObject {

	public enum Type {
		UPDATED,
		LOADED,
		SAVED
	}

	private Type type;

	private String root;

	private String key;

	private String oldValue;

	private String newValue;

	public SettingsEvent( Object source, Type type, String root ) {
		this( source, type, root, null, null, null );
	}

	public SettingsEvent( Object source, Type type, String root, String key, String oldValue, String newValue ) {
		super( source );
		this.type = type;
		this.root = root;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Type getType() {
		return type;
	}

	public String getRoot() {
		return root;
	}

	public String getKey() {
		return key;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( getClass().getSimpleName() );
		builder.append( ":" );
		builder.append( type );
		if( root != null ) {
			builder.append( ":" );
			builder.append( root );
		}
		if( key != null ) {
			builder.append( ":" );
			builder.append( key );
		}
		return builder.toString();
	}

}
