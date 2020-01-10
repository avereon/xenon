package com.avereon.xenon.node;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NodeEventTest {

	@Test
	void testEquals() {
		Node node = new MockNode();
		NodeEvent event1 = new NodeEvent( node, NodeEvent.NODE_CHANGED );
		NodeEvent event2 = new NodeEvent( node, NodeEvent.NODE_CHANGED );

		assertThat( event1.equals( event2 ), is( true ) );
		assertThat( event2.equals( event1 ), is( true ) );

		NodeEvent event3 = new NodeEvent( node, NodeEvent.VALUE_CHANGED, "a", null, "1" );
		NodeEvent event4 = new NodeEvent( node, NodeEvent.VALUE_CHANGED, "a", "1", "5" );
		assertThat( event3.equals( event4 ), is( true ) );
		assertThat( event4.equals( event3 ), is( true ) );
	}

}
