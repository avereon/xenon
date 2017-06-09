package com.parallelsymmetry.essence.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node {

	/**
	 * The collection of edges this node is associated with. The node may be the
	 * source or may be the target of the edge.
	 */
	private Set<Edge> edges;

	private Map<String, Object> values;

	public Set<Edge> getLinks() {
		return new HashSet<>( edges );
	}

	public Edge add( Node target ) {
		return add( target, false );
	}

	public Edge add( Node target, boolean directed ) {
		Edge edge = new Edge( this, target, directed );
		addEdge( edge );
		target.addEdge( edge );
		return edge;
	}

	public void remove( Node target ) {
		// Find all edges where target is a source or target
		for( Edge edge : findEdges( this.edges, this, target  ) ) {
			edge.getSource().removeEdge( edge );
			edge.getTarget().removeEdge( edge );
		}

	}

	public void setValue( String key, Object value ) {
		if( key == null ) throw new NullPointerException( "Key cannot be null" );
		if( value == null ) {
			if( values == null ) return;
			values.remove( key );
			if( values.size() == 0 ) values = null;
		} else {
			if( values == null ) this.values = new ConcurrentHashMap<>();
			values.put( key, value );
		}
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getValue( String key ) {
		return (T)values.get( key );
	}

	void addEdge( Edge edge ) {
		if( edges == null ) edges= new CopyOnWriteArraySet<>( );
		edges.add( edge );
	}

	void removeEdge( Edge edge ) {
		edges.remove( edge );
	}

	private Set<Edge> findEdges( Set<Edge> edges, Node source, Node target ) {
		Set<Edge> result = new HashSet<>(  );

		for( Edge edge : edges ) {
			if( edge.getSource() == source && edge.getTarget() == target ) result.add( edge );
			if( edge.getTarget() == source && edge.getSource() == target ) result.add( edge );
		}

		return result;
	}

}
