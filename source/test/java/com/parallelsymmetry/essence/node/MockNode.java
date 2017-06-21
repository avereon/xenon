package com.parallelsymmetry.essence.node;

public class MockNode extends Node {

	MockNode() {
		this( null );
	}

	MockNode( String name ) {
		definePrimaryKey( "name" );
		setName( name );
		setModified( false );
	}

	private String getName() {
		return getValue( "name" );
	}

	private void setName( String name ) {
		setValue( "name", name );
	}

}
