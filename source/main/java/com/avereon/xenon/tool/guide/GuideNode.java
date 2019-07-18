package com.avereon.xenon.tool.guide;

import com.avereon.xenon.node.Node;

public class GuideNode extends Node {

	public static final String ID = "id";

	public static final String ICON = "icon";

	public static final String NAME = "name";

	public GuideNode() {
		definePrimaryKey( ID );
		defineNaturalKey( NAME );
	}

	public GuideNode init( String id, String name ) {
		setId( id );
		setName( name );
		return this;
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public String getIcon() {
		return getValue( ICON );
	}

	public void setIcon( String name ) {
		setValue( ICON, name );
	}

	public String getName() {
		return getValue( NAME );
	}

	public void setName( String name ) {
		setValue( NAME, name );
	}

	public String toString() {
		return getName();
	}

}
