package com.avereon.xenon.undo;

import com.avereon.data.IdNode;

import java.util.Set;

public class MockNode extends IdNode {

	private static final String NODES = "nodes";

	public MockNode() {
		super();
	}

	public MockNode(String id) {
		super();
		setId( id );
	}

	public MockNode addNode( MockNode shape ) {
		addToSet( NODES, shape );
		return this;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	public MockNode removeNode( MockNode shape ) {
		removeFromSet( NODES, shape );
		return this;
	}

	public Set<MockNode> getNodes() {
		return getValues( NODES );
	}

}
