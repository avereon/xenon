package com.parallelsymmetry.essence.node;

import com.parallelsymmetry.essence.data.CircularReferenceException;
import com.parallelsymmetry.essence.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node implements TxnEventDispatcher {

	private static final Logger log = LoggerFactory.getLogger( Node.class );

	static final String MODIFIED = "flag.modified";

	// A special object to represent null values
	private static final Object NULL = new Object();

	/**
	 * The parent of the node.
	 */
	private Node parent;

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

	private boolean modified;

	private boolean selfModified;

	private boolean treeModified;

	private int modifiedChildCount;

	private Map<String, Object> modifiedValues;

	/**
	 * Is the node modified. The node is modified if any data value has been
	 * modified or any child node has been modified since the last time
	 * setModified( false ) was called.
	 *
	 * @return true if this node or any child nodes are modified, false otherwise.
	 */
	public boolean isModified() {
		return modified;
	}

	public void setModified( boolean modified ) {
		setFlag( MODIFIED, modified );
	}

	//	public Set<Edge> getLinks() {
	//		return new HashSet<>( edges );
	//	}
	//
	//	public Edge add( Node target ) {
	//		return add( target, false );
	//	}
	//
	//	public Edge add( Node target, boolean directed ) {
	//		Edge edge = new Edge( this, target, directed );
	//		addEdge( edge );
	//		target.addEdge( edge );
	//		return edge;
	//	}
	//
	//	public void remove( Node target ) {
	//		// Find all edges where target is a source or target
	//		for( Edge edge : findEdges( this.edges, this, target ) ) {
	//			edge.getSource().removeEdge( edge );
	//			edge.getTarget().removeEdge( edge );
	//		}
	//
	//	}

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

	@Override
	public void dispatchEvent( TxnEvent event ) {
		if( !(event instanceof NodeEvent) ) return;

		if( listeners != null ) {
			for( NodeListener listener : listeners ) {
				listener.eventOccurred( (NodeEvent)event );
			}
		}
	}

	public Collection<NodeListener> getNodeListeners() {
		return listeners == null ? new HashSet<>() : new HashSet<>( listeners );
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
		if( keys != null ) {
			for( String key : keys ) {
				Object value = getValue( key );
				if( value == null ) continue;
				if( !first ) builder.append( "," );
				builder.append( key );
				builder.append( "=" );
				builder.append( value );
				first = false;
			}
		}
		builder.append( "]" );

		return builder.toString();
	}

	@Override
	public boolean equals( Object object ) {
		if( object == null || this.getClass() != object.getClass() ) return false;

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

	protected int getModifiedValueCount() {
		return modifiedValues == null ? 0 : modifiedValues.size();
	}

	@SuppressWarnings( "unchecked" )
	protected <T> T getValue( String key ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );

		return values == null ? null : (T)values.get( key );
	}

	protected void setValue( String key, Object newValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );

		Object oldValue = getValue( key );
		if( newValue == oldValue ) return;

		try {
			Txn.create();
			Txn.submit( new SetValueOperation( this, key, oldValue, newValue ) );
			Txn.submitAfter( new UpdateModifiedFlagOperation( this ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.error( "Error setting flag: " + key, exception );
		}

	}

	protected boolean getFlag( String key ) {
		if( key == null ) throw new NullPointerException( "Flag key cannot be null" );
		return flags != null && flags.contains( key );
	}

	protected void setFlag( String key, boolean newValue ) {
		if( key == null ) throw new NullPointerException( "Flag key cannot be null" );

		boolean oldValue = getFlag( key );
		if( newValue == oldValue ) return;

		try {
			Txn.create();
			Txn.submit( new SetFlagOperation( this, key, oldValue, newValue ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.error( "Error setting flag: " + key, exception );
		}
	}

	Node getParent() {
		return parent;
	}

	void setParent( Node parent ) {
		checkForCircularReference( parent );
		this.parent = parent;
	}

	List<Node> getNodePath() {
		return getNodePath( null );
	}

	List<Node> getNodePath( Node stop ) {
		List<Node> path = new ArrayList<>();
		if( this != stop && parent != null ) path = parent.getNodePath();
		path.add( this );
		return path;
	}

	void childModified( boolean modified ) {
		modifiedChildCount = modified ? modifiedChildCount + 1 : modifiedChildCount - 1;
		updateModified( selfModified, modifiedChildCount );
	}

	void addEdge( Edge edge ) {
		if( edges == null ) edges = new CopyOnWriteArraySet<>();
		edges.add( edge );
	}

	void removeEdge( Edge edge ) {
		edges.remove( edge );
	}

	private void checkForCircularReference( Node node ) {
		Node parent = this;
		while( parent != null ) {
			if( node == parent ) throw new CircularReferenceException( "Circular reference detected in parent path: " + node );
			parent = parent.getParent();
		}
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

	private void updateModified( boolean selfModified, int modifiedChildCount ) {
		modified = selfModified || modifiedChildCount != 0;
		System.out.println( this.toString( true ) + "  mcc=" + modifiedChildCount + "  modified="+modified );
		if( !selfModified ) modifiedValues = null;
	}

	private void doSetFlag( String key, boolean newValue ) {
		if( newValue ) {
			if( flags == null ) flags = new CopyOnWriteArraySet<>();
			flags.add( key );
		} else {
			if( flags != null ) {
				flags.remove( key );
				if( flags.size() == 0 ) flags = null;
			}
		}

		if( MODIFIED.equals( key ) ) updateModified( newValue, modifiedChildCount );

		// Propagate the flag value to parent
		Node parent = getParent();
		if( parent != null ) parent.childModified( newValue );

		//			// Propagate the flag value to children
		//			if( newValue == false ) {
		//				// Clear the modified flag of any child nodes
		//				if( values != null ) {
		//					for( Object value : values.values() ) {
		//						if( value instanceof Node ) {
		//							Node child = (Node)value;
		//							if( child.isModified() ) child.setModified( false );
		//						}
		//					}
		//				}
		//			}

	}

	private void doSetValue( String key, Object oldValue, Object newValue ) {
		if( newValue == null ) {
			if( values == null ) return;
			values.remove( key );
			if( values.size() == 0 ) values = null;
			if( oldValue instanceof Node ) ((Node)oldValue).setParent( null );
		} else {
			if( values == null ) values = new ConcurrentHashMap<>();
			values.put( key, newValue );
			if( newValue instanceof Node ) ((Node)newValue).setParent( this );
		}
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
			setValue( key, oldValue, newValue );
		}

		@Override
		protected void revert() throws TxnException {
			setValue( key, newValue, oldValue );
		}

		private void setValue( String key, Object oldValue, Object newValue ) {
			doSetValue( key, oldValue, newValue );

			// Update the modified value map
			Object preValue = modifiedValues == null ? null : modifiedValues.get( key );
			if( preValue == null ) {
				// Only add the value if there is not an existing previous value
				if( modifiedValues == null ) modifiedValues = new ConcurrentHashMap<>();
				modifiedValues.put( key, oldValue == null ? NULL : oldValue );
			} else if( Objects.equals( preValue == NULL ? null : preValue, newValue ) ) {
				if( modifiedValues != null ) {
					modifiedValues.remove( key );
					if( modifiedValues.size() == 0 ) modifiedValues = null;
				}
			}

			NodeEvent.Type type = NodeEvent.Type.VALUE_CHANGED;
			// FIXME Enable value insert and remove events
			//			type = oldValue == null ? NodeEvent.Type.VALUE_INSERT : type;
			//			type = newValue == null ? NodeEvent.Type.VALUE_REMOVE : type;

			getResult().addEvent( new NodeEvent( getNode(), type, key, oldValue, newValue ) );

			Node parent = Node.this.getParent();
			if( parent != null ) getResult().addEvent( new NodeEvent( parent, getNode(), type, key, oldValue, newValue ) );
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
			setFlag( key, oldValue, newValue );
		}

		@Override
		protected void revert() throws TxnException {
			setFlag( key, newValue, oldValue );
		}

		private void setFlag( String key, boolean oldValue, boolean newValue ) {
			doSetFlag( key, newValue );
			getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.FLAG_CHANGED, key, oldValue, newValue ) );
			getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.NODE_CHANGED ) );
		}

	}

	private class UpdateModifiedFlagOperation extends NodeTxnOperation {

		private boolean oldValue;

		UpdateModifiedFlagOperation( Node node ) {
			super( node );
			oldValue = node.isModified();
		}

		@Override
		protected void commit() throws TxnException {
			boolean newValue = modifiedValues != null && modifiedValues.size() > 0;
			if( newValue != oldValue ) {
				doSetFlag( MODIFIED, newValue );
				getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.FLAG_CHANGED, MODIFIED, oldValue, newValue ) );
			}
			getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.NODE_CHANGED, MODIFIED, oldValue, newValue ) );

			Node parent = getNode().getParent();
			if( parent != null ) getResult().addEvent( new NodeEvent( parent, getNode(), NodeEvent.Type.NODE_CHANGED ) );
		}

		@Override
		protected void revert() throws TxnException {
			doSetFlag( MODIFIED, oldValue );
		}

	}

}
