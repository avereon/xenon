package com.xeomar.xenon.node;

import org.junit.Before;

public class EdgeTest {

	private Node source;

	private Node target;

	private Edge edge;

	@Before
	public void setup() {
		source = new Node();
		target = new Node();
		//edge = source.add( target );
	}

//	@Test
//	public void testFields() {
//		assertThat( edge.getSource(), is( source ) );
//		assertThat( edge.getTarget(), is( target ) );
//		assertThat( edge.isDirected(), is( false ) );
//	}
//
//	@Test
//	public void testValues() {
//		edge.setValue( "name", "Test" );
//
//		assertThat( edge.getValue( "name" ), is( "Test" ));
//	}

}
