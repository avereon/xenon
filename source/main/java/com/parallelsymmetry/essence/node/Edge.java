package com.parallelsymmetry.essence.node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Edge {

	private Map<String, Object> values;

	private Node source;

	private Node target;

	private boolean bidirectional;

	Edge( Node source, Node target, boolean bidirectional ) {
		if( source == null ) throw new NullPointerException( "Source cannot be null" );
		if( target == null ) throw new NullPointerException( "Target cannot be null" );
		this.source = source;
		this.target = target;
		this.bidirectional = bidirectional;
	}

	public Node getSource() {
		return source;
	}

	public Node getTarget() {
		return target;
	}

	public boolean isBidirectional() {
		return bidirectional;
	}

	public void setBidirectional( boolean bidirectional ) {
		this.bidirectional = bidirectional;
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

}
