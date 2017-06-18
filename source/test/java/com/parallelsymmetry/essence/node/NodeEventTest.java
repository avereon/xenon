package com.parallelsymmetry.essence.node;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NodeEventTest {

	@Test
	public void testEquals() {
		Node node = new MockNode();
		NodeEvent event1 = new NodeEvent( node, NodeEvent.Type.NODE_CHANGED );
		NodeEvent event2 = new NodeEvent( node, NodeEvent.Type.NODE_CHANGED );

		assertThat( event1.equals( event2 ), is( true ) );
		assertThat( event2.equals( event1 ), is( true ) );
	}

}
