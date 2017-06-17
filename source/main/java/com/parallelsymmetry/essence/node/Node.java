package com.parallelsymmetry.essence.node;

import com.parallelsymmetry.essence.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node implements TxnEventDispatcher {

	private static final Logger log = LoggerFactory.getLogger( Node.class );

	private static final String MODIFIED = "flag.modified";

	/**
	 * The node flags.
	 */
	private Set<String> flags;

	/**
	 * The node values.
	 */
	private Map<String, Object> values;

	/**
	 * The node resources. This map provides a way to associate objects without
	 * affecting the data model as a whole. Adding or removing resources does not
	 * affect the node state nor does it cause any kind of event. This is simply
	 * a storage mechanism.
	 */
	private Map<String, Object> resources;

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

	/**
	 * The set of node listeners.
	 */
	private Set<NodeListener> listeners;

	private int modifiedValueCount;

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
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );

		Object oldValue = getValue( key );
		if( value == oldValue ) return;

		try {
			Txn.create();
			Txn.submit( new SetValueOperation( this, key, oldValue, value ) );
			Txn.commit();

			// TODO Cannot set modified flag until state is updated
			// but it would be nice if it were part of the transaction
			setFlag( MODIFIED, calculateStateHash() != lastUnmodifiedStateHash );
		} catch( TxnException exception ) {
			log.error( "Error setting flag: " + key, exception );
		}

	}

	public int getModifiedValueCount() {
		return modifiedValueCount;
	}

	public <T> void putResource( String key, T value ) {
		if( value == null ) {
			if( resources != null ) {
				resources.remove( key );
				if( resources.size() == 0 ) resources = null;
			}
		} else {
			if( resources == null ) resources = new ConcurrentHashMap<>();
			resources.put( key, value );
		}
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getResource( String key ) {
		return resources == null ? null : (T)resources.get( key );
	}

	public boolean isModified() {
		return getFlag( MODIFIED );
	}

	public void setModified( boolean modified ) {
		setFlag( MODIFIED, modified );
		if( !modified ) lastUnmodifiedStateHash = calculateStateHash();
	}

	protected boolean getFlag( String key ) {
		return flags != null && flags.contains( key );
	}

	protected void setFlag( String key, boolean value ) {
		if( key == null ) throw new NullPointerException( "Flag key cannot be null" );

		boolean oldValue = getFlag( key );
		if( value == oldValue ) return;

		try {
			Txn.create();
			Txn.submit( new SetFlagOperation( this, key, oldValue, value ) );
			// Propagate the value to parent
			// Propagate the value to children
			Txn.commit();
		} catch( TxnException exception ) {
			log.error( "Error setting flag: " + key, exception );
		}
	}

	@Override
	public void dispatchEvent( TxnEvent event ) {
		if( listeners == null || !(event instanceof NodeEvent) ) return;
		for( NodeListener listener : listeners ) {
			listener.eventOccurred( (NodeEvent)event );
		}
	}

	public synchronized void addNodeListener( NodeListener listener ) {
		if( listeners == null ) listeners = new CopyOnWriteArraySet<>();
		listeners.add( listener );
	}

	public synchronized void removeNodeListener( NodeListener listener ) {
		listeners.remove( listener );
		if( listeners.size() == 0 ) listeners = null;
	}

	@Override
	public String toString() {
		return toString( false );
	}

	public String toString( boolean allValues ) {
		StringBuilder builder = new StringBuilder();

		List<String> keys = businessKeyList;
		if( allValues ) {
			keys = new ArrayList<>();
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

	protected void definePrimaryKey( String... keys ) {
		if( primaryKeyList == null ) {
			primaryKeyList = Collections.unmodifiableList( Arrays.asList( keys ) );
		} else {
			throw new IllegalStateException( "Primary key already set" );
		}
	}

	protected void defineBusinessKey( String... keys ) {
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

		int hash = 281;
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

	private abstract class NodeTxnOperation extends TxnOperation {

		private Node node;

		NodeTxnOperation( Node node ) {
			this.node = node;
		}

		protected Node getNode() {
			return node;
		}

	}

	private class SetValueOperation extends NodeTxnOperation {

		private String key;

		private Object oldValue;

		private Object newValue;

		SetValueOperation( Node node, String key, Object oldValue, Object newValue ) {
			super( node );
			this.key = key;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		protected void commit() throws TxnException {
			setValue( key, newValue );
		}

		@Override
		protected void revert() throws TxnException {
			setValue( key, oldValue );
		}

		private void setValue( String key, Object value ) {
			if( value == null ) {
				if( values == null ) return;
				values.remove( key );
				if( values.size() == 0 ) values = null;
			} else {
				if( values == null ) values = new ConcurrentHashMap<>();
				values.put( key, value );
			}
			getResult().addEvent( new NodeEvent( Node.this, NodeEvent.Type.VALUE_CHANGED, oldValue, newValue ) );
			getResult().addEvent( new NodeEvent( Node.this, NodeEvent.Type.NODE_CHANGED ) );
		}

	}

	private class SetFlagOperation extends NodeTxnOperation {

		private String key;

		private boolean oldValue;

		private boolean newValue;

		SetFlagOperation( Node node, String key, boolean oldValue, boolean newValue ) {
			super( node );
			this.key = key;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		protected void commit() throws TxnException {
			setValue( key, newValue );
		}

		@Override
		protected void revert() throws TxnException {
			setValue( key, oldValue );
		}

		private void setValue( String key, boolean value ) {
			if( value ) {
				if( flags == null ) flags = new CopyOnWriteArraySet<>();
				flags.add( key );
			} else {
				if( flags != null ) {
					flags.remove( key );
					if( flags.size() == 0 ) flags = null;
				}
			}
			getResult().addEvent( new NodeEvent( Node.this, NodeEvent.Type.FLAG_CHANGED, oldValue, newValue ) );
			getResult().addEvent( new NodeEvent( Node.this, NodeEvent.Type.NODE_CHANGED ) );
		}

	}

}
