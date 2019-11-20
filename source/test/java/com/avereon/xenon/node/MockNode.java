package com.avereon.xenon.node;

class MockNode extends Node {

	MockNode() {
		this( null );
	}

	MockNode( String id ) {
		definePrimaryKey( "id" );
		setId( id );
		setModified( false );
	}

	private String getId() {
		return getValue( "id" );
	}

	private void setId( String id ) {
		setValue( "id", id );
	}

}
