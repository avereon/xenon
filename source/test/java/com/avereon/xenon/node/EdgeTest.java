package com.avereon.xenon.node;

import org.junit.jupiter.api.BeforeEach;

public class EdgeTest {

	private Node source;

	private Node target;

	private Edge edge;

	@BeforeEach
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
