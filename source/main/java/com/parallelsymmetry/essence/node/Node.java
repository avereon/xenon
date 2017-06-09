package com.parallelsymmetry.essence.node;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node {

	private Set<Edge> edges;

	private Map<String, Object> values;

	public Edge link( Node target ) {
		return link( target, false );
	}

	public Edge link( Node target, boolean bidirectional ) {
		Edge edge = new Edge( this, target, false );
		addEdge( edge );
		target.addEdge( edge );
		return edge;
	}

	public void unlink( Node target ) {

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
		// Could be source or target for the edge
		if( edges == null ) edges= new CopyOnWriteArraySet<>( );
		edges.add( edge );
	}

	void removeEdge( Edge edge ) {
		edges.remove( edge );
	}

}
