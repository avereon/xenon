package com.parallelsymmetry.essence.node;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EdgeTest {

	private Node source;

	private Node target;

	private Edge edge;

	@Before
	public void setup() {
		source = new Node();
		target = new Node();
		edge = source.link( target );
	}

	@Test
	public void testFields() {
		assertThat( edge.getSource(), is( source ) );
		assertThat( edge.getTarget(), is( target ) );
		assertThat( edge.isBidirectional(), is( false ) );
	}

	@Test
	public void testValues() {
		edge.setValue( "name", "Test" );

		assertThat( edge.getValue( "name" ), is( "Test" ));
	}

}
