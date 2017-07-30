package com.xeomar.xenon.node;

public interface DataFilter<T> {

	/**
	 * Test the specified node.
	 *
	 * @param node The node to test.
	 * @return True if the node should be accepted, false otherwise.
	 */
	public boolean accept( T node );

}
