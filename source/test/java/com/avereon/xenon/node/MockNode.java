package com.avereon.xenon.node;

class MockNode extends Node {

	private NodeWatcher watcher;

	MockNode() {
		this( null );
	}

	MockNode( String id ) {
		definePrimaryKey( "id" );
		setId( id );
		setModified( false );
		addNodeListener( watcher = new NodeWatcher() );
	}

	private String getId() {
		return getValue( "id" );
	}

	private void setId( String id ) {
		setValue( "id", id );
	}

	public NodeWatcher getWatcher() {
		return watcher;
	}

	int getEventCount() {
		return watcher.getEvents().size();
	}

}
