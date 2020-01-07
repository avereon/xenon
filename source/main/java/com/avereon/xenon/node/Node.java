package com.avereon.xenon.node;

import com.avereon.util.LogUtil;
import com.avereon.xenon.transaction.*;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class Node implements TxnEventDispatcher, Cloneable {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	/**
	 * The modified flag key.
	 */
	public static final String MODIFIED = "flag.modified";

	/**
	 * A special object to represent previous null values in the modifiedValues map.
	 */
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
	 * The node resources. This map provides a way to associate objects without affecting the data model as a whole. Adding or removing resources does not affect
	 * the node state nor does it cause any kind of event. This is simply a storage
	 * mechanism.
	 */
	private Map<String, Object> resources;

	/**
	 * The collection of edges this node is associated with. The node may be the source or may be the target of the edge.
	 */
	private Set<Edge> edges;

	/**
	 * The list of value keys that specify the primary key.
	 */
	private List<String> primaryKeyList;

	/**
	 * The list of value keys that specify the natural key.
	 */
	private List<String> naturalKeyList;

	/**
	 * The set of value keys that are read only.
	 */
	private Set<String> readOnlySet;

	/**
	 * The set of node listeners.
	 */
	private Set<NodeListener> listeners;

	/**
	 * The internally calculated modified flag used to allow for fast read rates. This is updated only when values or the modified flag is changed.
	 */
	private boolean modified;

	/**
	 * The map of previous values as the node is modified. This map is set to null when the modified flag is set to false.
	 */
	private Map<String, Object> modifiedValues;

	/**
	 * The count of child nodes that are modified.
	 */
	private Set<Node> modifiedChildren;

	/**
	 * Is the node modified. The node is modified if any data value has been modified or any child node has been modified since the last time setModified( false )
	 * was called.
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

	public Set<String> getResourceKeys() {
		return resources == null ? Collections.emptySet() : resources.keySet();
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getResource( String key ) {
		return resources == null ? null : (T)resources.get( key );
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

	@Override
	public void dispatchEvent( TxnEvent event ) {
		if( !(event instanceof NodeEvent) ) return;

		if( listeners != null ) {
			for( NodeListener listener : listeners ) {
				listener.nodeEvent( (NodeEvent)event );
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

	/**
	 * Copy the values and resources from the specified node. This method will only fill in missing values and resources from the specified node.
	 *
	 * @param node
	 */
	public <T extends Node> T copyFrom( Node node ) {
		return copyFrom( node, false );
	}

	/**
	 * Copy the values and resources from the specified node. If overwrite is true this method will replace any values or resources with the specified nodes
	 * values and resources. Otherwise, this method will only fill in missing values and
	 * resources from the specified node.
	 *
	 * @param node
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends Node> T copyFrom( Node node, boolean overwrite ) {
		// Clone values
		for( String key : node.getValueKeys() ) {
			if( overwrite || getValue( key ) == null ) setValue( key, node.getValue( key ) );
		}

		// Clone resources
		for( String key : node.getResourceKeys() ) {
			if( overwrite || getResource( key ) == null ) putResource( key, node.getResource( key ) );
		}

		return (T)this;
	}

	@Override
	public String toString() {
		return toString( false );
	}

	public String toString( boolean allValues ) {
		StringBuilder builder = new StringBuilder();

		List<String> keys = new ArrayList<>();
		if( primaryKeyList != null ) keys.addAll( primaryKeyList );
		if( naturalKeyList != null ) keys.addAll( naturalKeyList );
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
		if( naturalKeyList != null ) {
			for( String key : naturalKeyList ) {
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
		if( naturalKeyList != null ) {
			for( String key : naturalKeyList ) {
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

	protected void defineNaturalKey( String... keys ) {
		if( naturalKeyList == null ) {
			naturalKeyList = Collections.unmodifiableList( Arrays.asList( keys ) );
		} else {
			throw new IllegalStateException( "Natural key already set" );
		}
	}

	protected void defineReadOnly( String... keys ) {
		if( readOnlySet == null ) {
			readOnlySet = Collections.unmodifiableSet( new HashSet<>( Arrays.asList( keys ) ) );
		} else {
			throw new IllegalStateException( "Read only keys already set" );
		}
	}

	protected boolean isReadOnly( String key ) {
		return readOnlySet != null && readOnlySet.contains( key );
	}

	protected boolean getFlag( String key ) {
		if( key == null ) throw new NullPointerException( "Flag key cannot be null" );
		return flags != null && flags.contains( key );
	}

	protected void setFlag( String key, boolean newValue ) {
		if( key == null ) throw new NullPointerException( "Flag key cannot be null" );
		if( readOnlySet != null && readOnlySet.contains( key ) ) throw new IllegalStateException( "Attempt to set read-only flag: " + key );

		boolean oldValue = getFlag( key );

		try {
			Txn.create();
			Txn.submit( new SetFlagOperation( this, key, oldValue, newValue ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.error( "Error setting flag: " + key, exception );
		}
	}

	protected Set<String> getValueKeys() {
		return values == null ? Collections.emptySet() : values.keySet();
	}

	@SuppressWarnings( "unchecked" )
	protected <T> Collection<T> getValues( Class<T> clazz ) {
		return (Collection<T>)getValueKeys().stream().map( this::getValue ).filter( clazz::isInstance ).collect( Collectors.toUnmodifiableSet() );
	}

	protected <T> T getValue( String key ) {
		return getValue( key, null );
	}

	@SuppressWarnings( "unchecked" )
	protected <T> T getValue( String key, T defaultValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );

		T value = values == null ? null : (T)values.get( key );

		return value != null ? value : defaultValue;
	}

	protected void setValue( String key, Object newValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );
		if( readOnlySet != null && readOnlySet.contains( key ) ) throw new IllegalStateException( "Attempt to set read-only value: " + key );

		Object oldValue = getValue( key );

		try {
			Txn.create();
			Txn.submit( new SetValueOperation( this, key, oldValue, newValue ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.error( "Error setting flag: " + key, exception );
		}
	}

	protected void clear() {
		try {
			Txn.create();
			getValueKeys().forEach( k -> setValue( k, null ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.error( "Error clearing values", exception );
		}
	}

	int getModifiedValueCount() {
		return (modifiedValues == null ? 0 : modifiedValues.size());
	}

	int getModifiedChildCount() {
		return modifiedChildren == null ? 0 : modifiedChildren.size();
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

	private List<Node> getNodePath( Node stop ) {
		List<Node> path = new ArrayList<>();
		if( this != stop && parent != null ) path = parent.getNodePath();
		path.add( this );
		return path;
	}

	void addEdge( Edge edge ) {
		if( edges == null ) edges = new CopyOnWriteArraySet<>();
		edges.add( edge );
	}

	void removeEdge( Edge edge ) {
		edges.remove( edge );
	}

	private void doSetFlag( String key, boolean newValue ) {
		flags = updateSet( flags, key, newValue );

		if( MODIFIED.equals( key ) ) {
			if( !newValue ) {
				modifiedValues = null;
				modifiedChildren = null;
			}
			updateModified();
		}
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

		updateModified();
	}

	private void updateModified() {
		log.warn( "modified flag: " + getFlag( MODIFIED ) + " modified children: " + (getModifiedChildCount() != 0) );
		modified = getFlag( MODIFIED ) || getModifiedChildCount() != 0;
	}

	private boolean childModified( Node child, boolean modified ) {
		boolean previousModified = isModified();

		// Search the value map for the modified child
		for( String key : values.keySet() ) {
			Object value = values.get( key );
			if( value != child ) continue;
			modifiedChildren = updateSet( modifiedChildren, child, modified );
		}

		updateModified();

		return isModified() != previousModified;
	}

	private <T> Set<T> updateSet( Set<T> set, T child, boolean newValue ) {
		if( newValue ) {
			if( set == null ) set = new CopyOnWriteArraySet<>();
			set.add( child );
		} else {
			if( set != null ) {
				set.remove( child );
				if( set.size() == 0 ) set = null;
			}
		}

		return set;
	}

	private void checkForCircularReference( Node node ) {
		Node parent = this;
		while( parent != null ) {
			if( node == parent ) throw new CircularReferenceException( "Circular reference detected in parent path: " + node );
			parent = parent.getParent();
		}
	}

	@Deprecated
	private Set<Edge> findEdges( Set<Edge> edges, Node source, Node target ) {
		Set<Edge> result = new HashSet<>();

		for( Edge edge : edges ) {
			if( edge.getSource() == source && edge.getTarget() == target ) result.add( edge );
			if( edge.getTarget() == source && edge.getSource() == target ) result.add( edge );
		}

		return result;
	}

	@Deprecated
	private int calculateStateHash() {
		if( values == null ) return 0;

		int hash = 281;
		for( String key : values.keySet() ) {
			Object value = values.get( key );
			hash ^= value.hashCode();
		}
		return hash;
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
			boolean currentValue = getFlag( key );

			// This operation must be created before any changes are made
			UpdateModifiedOperation updateModified = new UpdateModifiedOperation( Node.this );

			// Even if the flag value does not change, doSetFlag should be called
			doSetFlag( key, newValue );

			if( newValue != currentValue ) {
				getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.FLAG_CHANGED, key, oldValue, newValue ) );
				getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.NODE_CHANGED ) );
				Txn.submit( updateModified );
			}

			// Propagate the flag value to children
			if( values != null && MODIFIED.equals( key ) && !newValue ) {
				// Clear the modified flag of any child nodes
				for( Object value : values.values() ) {
					if( value instanceof Node ) {
						Node child = (Node)value;
						if( child.isModified() ) {
							child.doSetFlag( key, false );
							getResult().addEvent( new NodeEvent( child, NodeEvent.Type.FLAG_CHANGED, key, true, false ) );
							getResult().addEvent( new NodeEvent( child, NodeEvent.Type.NODE_CHANGED ) );
						}
					}
				}
			}
		}

		@Override
		protected void revert() throws TxnException {
			doSetFlag( key, oldValue );
		}

		@Override
		public String toString() {
			return "set flag  " + key + " " + oldValue + " -> " + newValue;
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
			Object currentValue = getValue( key );
			if( newValue == currentValue ) return;

			// This operation must be created before any changes are made
			UpdateModifiedOperation updateModified = new UpdateModifiedOperation( Node.this );

			setValue( key, oldValue, newValue );

			Node parent = getParent();
			NodeEvent.Type type = NodeEvent.Type.VALUE_CHANGED;
			// FIXME Enable value insert and remove events
			//type = oldValue == null ? NodeEvent.Type.VALUE_INSERT : type;
			//type = newValue == null ? NodeEvent.Type.VALUE_REMOVE : type;

			// Send an event to the node about the value change
			getResult().addEvent( new NodeEvent( getNode(), type, key, oldValue, newValue ) );

			// Send an event to the parent about the value change
			if( parent != null ) getResult().addEvent( new NodeEvent( parent, getNode(), type, key, oldValue, newValue ) );

			Txn.submit( updateModified );
		}

		@Override
		protected void revert() throws TxnException {
			setValue( key, newValue, oldValue );
		}

		@Override
		public String toString() {
			return "set value " + key + " " + oldValue + " -> " + newValue;
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
		}

	}

	private class UpdateModifiedOperation extends NodeTxnOperation {

		private boolean oldValue;

		UpdateModifiedOperation( Node node ) {
			super( node );
			// Check only the modified values
			oldValue = hasModifiedValues();
		}

		@Override
		protected void commit() throws TxnException {
			// Check only the modified values
			boolean newValue = hasModifiedValues();

			// Check if the modified values should change the modified flag
			if( newValue != oldValue ) {
				doSetFlag( MODIFIED, newValue );
				getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.FLAG_CHANGED, MODIFIED, oldValue, newValue ) );
			}

			// Add the node changed event
			getResult().addEvent( new NodeEvent( getNode(), NodeEvent.Type.NODE_CHANGED ) );

			// Check all the parents for modification
			Node node = getNode();
			Node parent = getNode().getParent();
			while( parent != null ) {
				boolean priorModified = parent.isModified();
				boolean parentChanged = parent.childModified( node, newValue );
				if( parentChanged ) getResult().addEvent( new NodeEvent( parent, NodeEvent.Type.FLAG_CHANGED, MODIFIED, priorModified, !priorModified ) );
				getResult().addEvent( new NodeEvent( parent, node, NodeEvent.Type.NODE_CHANGED ) );
				node = parent;
				parent = parent.getParent();
			}
		}

		@Override
		protected void revert() throws TxnException {
			doSetFlag( MODIFIED, oldValue );
		}

		@Override
		public String toString() {
			return "update modified from " + oldValue;
		}

		private boolean hasModifiedValues() {
			return modifiedValues != null && modifiedValues.size() > 0;
		}

	}

}
