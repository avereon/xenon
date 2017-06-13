package com.parallelsymmetry.essence.node;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node {

	public static final String MODIFIED = "modified";

	/**
	 * The node flags.
	 */
	private Set<String> flags;

	/**
	 * The node values.
	 */
	private Map<String, Object> values;

	/**
	 * The collection of edges this node is associated with. The node may be the
	 * source or may be the target of the edge.
	 */
	private Set<Edge> edges;

	/**
	 * The list of value keys that specify the primary key.
	 */
	private List<String> primaryKeyList;

	/**
	 * The list of value keys that specify the business key.
	 */
	private List<String> businessKeyList;

	private int lastUnmodifiedStateHash;

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
		for( Edge edge : findEdges( this.edges, this, target ) ) {
			edge.getSource().removeEdge( edge );
			edge.getTarget().removeEdge( edge );
		}

	}

	@SuppressWarnings( "unchecked" )
	public <T> T getValue( String key ) {
		return values == null ? null : (T)values.get( key );
	}

	public void setValue( String key, Object value ) {
		if( key == null ) throw new NullPointerException( "Key cannot be null" );

		Object oldValue = getValue( key );
		Object newValue = value;

		if( value == null ) {
			if( values == null ) return;
			values.remove( key );
			if( values.size() == 0 ) values = null;
		} else {
			if( values == null ) this.values = new ConcurrentHashMap<>();
			values.put( key, value );
		}

		System.out.println( "lastHash=" +lastUnmodifiedStateHash + " stateHash=" + calculateStateHash() );
		doSetModified( calculateStateHash() != lastUnmodifiedStateHash );

		// TODO Add event to send
		//if( !Objects.equals( oldValue, newValue )) addValueChangedEvent( key, oldValue, newValue );
	}

	public boolean isModified() {
		return getFlag( MODIFIED );
	}

	public void setModified( boolean modified ) {
		doSetModified( modified );
		if( !modified ) lastUnmodifiedStateHash = calculateStateHash();
	}

	private void doSetModified( boolean modified ) {
		setFlag( MODIFIED, modified );
	}

	protected boolean getFlag( String key ) {
		return flags != null && flags.contains( key );
	}

	protected void setFlag( String key, boolean value ) {
		boolean oldValue = getFlag( key );
		boolean newValue = value;

		// Set the value
		if( value ) {
			if( flags == null ) flags = new CopyOnWriteArraySet<>();
			flags.add( key );
		} else {
			if( flags != null ) {
				flags.remove( key );
				if( flags.size() == 0 ) flags = null;
			}
		}

		// Propagate the value to parent
		// Propagate the value to children
	}

	@Override
	public String toString() {
		return toString( false );
	}

	public String toString( boolean allValues ) {
		StringBuilder builder = new StringBuilder();

		List<String> keys = businessKeyList;
		if( allValues ) {
			keys = new ArrayList<String>();
			if( values != null ) keys.addAll( values.keySet() );
			Collections.sort( keys );
		}

		boolean first = true;
		builder.append( getClass().getSimpleName() );
		builder.append( "[" );
		for( String key : keys ) {
			Object value = getValue( key );
			if( value == null ) continue;
			if( !first ) builder.append( "," );
			builder.append( key );
			builder.append( "=" );
			builder.append( value );
			first = false;
		}
		builder.append( "]" );

		return builder.toString();
	}

	@Override
	public boolean equals( Object object ) {
		if( this.getClass() != object.getClass() ) return false;

		Node that = (Node)object;
		if( primaryKeyList != null ) {
			for( String key : primaryKeyList ) {
				if( !Objects.equals( this.getValue( key ), that.getValue( key ) ) ) return false;
			}
		}
		if( businessKeyList != null ) {
			for( String key : businessKeyList ) {
				if( !Objects.equals( this.getValue( key ), that.getValue( key ) ) ) return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hashcode = 0;

		if( primaryKeyList != null ) {
			for( String key : primaryKeyList ) {
				Object value = getValue( key );
				if( value != null ) hashcode ^= value.hashCode();
			}
		}
		if( businessKeyList != null ) {
			for( String key : businessKeyList ) {
				Object value = getValue( key );
				if( value != null ) hashcode ^= value.hashCode();
			}
		}

		return hashcode;
	}

	protected void setPrimaryKey( String... keys ) {
		if( primaryKeyList == null ) {
			primaryKeyList = Collections.unmodifiableList( Arrays.asList( keys ) );
		} else {
			throw new IllegalStateException( "Primary key already set" );
		}
	}

	protected void setBusinessKey( String... keys ) {
		if( businessKeyList == null ) {
			businessKeyList = Collections.unmodifiableList( Arrays.asList( keys ) );
		} else {
			throw new IllegalStateException( "Business key already set" );
		}
	}

	void addEdge( Edge edge ) {
		if( edges == null ) edges = new CopyOnWriteArraySet<>();
		edges.add( edge );
	}

	void removeEdge( Edge edge ) {
		edges.remove( edge );
	}

	private int calculateStateHash() {
		if( values == null ) return 0;

		int hash = 0;
		for( String key : values.keySet() ) {
			Object value = values.get( key );
			hash ^= value.hashCode();
		}
		return hash;
	}

	private Set<Edge> findEdges( Set<Edge> edges, Node source, Node target ) {
		Set<Edge> result = new HashSet<>();

		for( Edge edge : edges ) {
			if( edge.getSource() == source && edge.getTarget() == target ) result.add( edge );
			if( edge.getTarget() == source && edge.getSource() == target ) result.add( edge );
		}

		return result;
	}

}
