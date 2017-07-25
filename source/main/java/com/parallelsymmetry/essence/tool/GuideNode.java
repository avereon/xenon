package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.node.Node;

public class GuideNode extends Node {

	public GuideNode() {
		definePrimaryKey( "id" );
		defineBusinessKey( "name" );
	}

	public String getId() {
		return getValue( "id" );
	}

	public void setId( String id ) {
		setValue( "id", id );
	}

	public String getName() {
		return getValue( "name" );
	}

	public void setName( String name ) {
		setValue( "name", name );
	}

	public String toString() {
		return getName();
	}

}
