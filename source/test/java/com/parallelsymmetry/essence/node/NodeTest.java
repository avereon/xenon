package com.parallelsymmetry.essence.node;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class NodeTest {

	private Node node;

	@Before
	public void setup() throws Exception {
		node = new Node();
	}

	@Test
	public void testLink() {
		Node target = new Node();
		Edge edge = node.add( target );

		assertThat( edge.isDirected(), is ( false ) );
		assertThat( node.getLinks(), contains( edge ) );
	}

	@Test
	public void testUnlink() {
		Node target = new Node();
		Edge edge = node.add( target );

		assertThat( edge.isDirected(), is ( false ) );
		assertThat( node.getLinks(), contains( edge ) );

		node.remove( edge.getTarget() );

		assertThat( node.getLinks(), not( contains( edge ) ) );
	}



}
