package com.parallelsymmetry.essence.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class DataNode {

	public static final String MODIFIED = "modified";

	private static final Object NULL = new Object();

	protected boolean modified;

	protected DataNode parent;

	private boolean selfModified;

	private int modifiedAttributeCount;

	private Map<String, Object> dataValues;

	private Map<String, Object> metaValues;

	private Map<String, Object> modifiedDataValues;

	private Map<String, Object> modifiedMetaValues;

	private Map<String, Object> resources;

	protected Set<DataListener> listeners = new CopyOnWriteArraySet<DataListener>();

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

	/**
	 * Set the modified flag for this node.
	 */
	public void setModified( boolean modified ) {
		if( this.modified == modified ) return;

		if( modified ) {
			setMetaValue( MODIFIED, true );
		} else {
			unmodify();
		}
	}

	/**
	 * Get an attribute value. Normally this method is not called directly but is
	 * wrapped by an attribute getter method. Example:
	 *
	 * <pre>
	 * public String getName() {
	 * 	return getAttribute( &quot;name&quot; );
	 * }
	 * </pre>
	 *
	 * Note: Be sure to handle primitives correctly by checking for null values:
	 *
	 * <pre>
	 * public int getLimit() {
	 * 	Integer result = getAttribute( &quot;limit&quot; );
	 * 	return result == null ? 0 : result;
	 * }
	 * </pre>
	 *
	 * @param <T> The return value type.
	 * @param name The attribute name.
	 * @return The attribute value or null if it does not exist.
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T getAttribute( String name ) {
		// Null attribute names are not allowed.
		if( name == null ) throw new NullPointerException( "Data value name cannot be null." );

		return (T)( dataValues == null ? null : dataValues.get( name ) );
	}

	/**
	 * Set an attribute value. Normally this method is not called directly but is
	 * wrapped by an attribute setter method. Example:
	 *
	 * <pre>
	 * public void setName( String string ) {
	 * 	setAttribute( &quot;name&quot;, string );
	 * }
	 * </pre>
	 *
	 * @param name The attribute name.
	 * @param newValue The attribute value.
	 */
	public void setAttribute( String name, Object newValue ) {
		// Null attribute names are not allowed.
		if( name == null ) throw new NullPointerException( "Data value name cannot be null." );

		// If the old value is equal to the new value no changes are necessary.
		Object oldValue = getAttribute( name );
		if( Objects.equals( oldValue, newValue ) ) return;

		if( newValue instanceof DataNode ) checkForCircularReference( (DataNode)newValue );

		Transaction.create();
		Transaction.submit( new SetAttributeOperation( this, name, oldValue, newValue ) );
		Transaction.commit();
	}

	public int getModifiedAttributeCount() {
		return modifiedAttributeCount;
	}

	/**
	 * Copy all attributes and resources from the specified node.
	 *
	 * @param node
	 */
	public void copy( DataNode node ) {
		for( String key : getAttributeKeys() ) {
			setAttribute( key, node.getAttribute( key ) );
		}
		for( String key : getResourceKeys() ) {
			putResource( key, node.getResource( key ) );
		}
	}

	/**
	 * Fill in any missing attributes and resources from the specified node.
	 *
	 * @param node
	 */
	public void fill( DataNode node ) {
		for( String key : node.getAttributeKeys() ) {
			if( getAttribute( key ) == null ) setAttribute( key, node.getAttribute( key ) );
		}
		for( String key : node.getResourceKeys() ) {
			if( getResource( key ) == null ) putResource( key, node.getResource( key ) );
		}
	}

	/**
	 * Get the set of attribute keys.
	 *
	 * @return The attribute key set.
	 */
	public Set<String> getAttributeKeys() {
		return dataValues == null ? new HashSet<String>() : dataValues.keySet();
	}

	/**
	 * Get the set of resource keys.
	 *
	 * @return The resource key set.
	 */
	public Set<String> getResourceKeys() {
		return resources == null ? new HashSet<String>() : resources.keySet();
	}

	/**
	 * Convenience method to get the parent when there is only one.
	 *
	 * @return The parent node or null if there is not a parent.
	 * @throws RuntimeException if there is more than one parent.
	 */
	public DataNode getParent() {
		return parent;
	}

	public List<DataNode> getNodePath() {
		return getNodePath( null );
	}

	public List<DataNode> getNodePath( DataNode stop ) {
		List<DataNode> path = new ArrayList<DataNode>();

		if( this != stop && parent != null ) path = parent.getNodePath();

		path.add( this );

		return path;
	}

	/**
	 * Get a stored resource. Putting or removing a resource will not modify the
	 * data.
	 *
	 * @param key
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T getResource( String key ) {
		if( resources == null ) return null;
		return (T)resources.get( key );
	}

	/**
	 * Store a resource. Putting or removing a resource will not modify the data.
	 * A resource is removed by setting the resource value to null.
	 *
	 * @param value
	 */
	public void putResource( String key, Object value ) {
		if( value == null ) {
			if( resources == null ) return;
			resources.remove( key );
			if( resources.size() == 0 ) resources = null;
		} else {
			if( resources == null ) resources = new ConcurrentHashMap<String, Object>();
			resources.put( key, value );
		}
	}

	public void addDataListener( DataListener listener ) {
		listeners.add( listener );
	}

	public void removeDataListener( DataListener listener ) {
		listeners.remove( listener );
	}

	//	@Override
	//	public boolean equals( Object object ) {
	//		return equalsUsingAttributes( object );
	//	}

	/**
	 * Compares the object using the class and attributes for equality testing.
	 */
	public boolean equalsUsingAttributes( Object object ) {
		if( !( object instanceof DataNode ) ) return false;

		DataNode that = (DataNode)object;

		Map<String, Object> thisAttr = this.dataValues;
		Map<String, Object> thatAttr = that.dataValues;

		if( thisAttr == null && thatAttr == null ) return true;
		if( thisAttr == null && thatAttr != null ) return false;
		if( thisAttr != null && thatAttr == null ) return false;

		if( thisAttr.size() != thatAttr.size() ) return false;

		Set<String> thisKeys = thisAttr.keySet();
		Set<String> thatKeys = thatAttr.keySet();
		for( String key : thisKeys ) {
			if( !thatKeys.contains( key ) ) return false;

			Object thisObject = thisAttr.get( key );
			Object thatObject = thatAttr.get( key );

			if( thisObject instanceof DataNode ) {
				if( !( (DataNode)thisObject ).equalsUsingAttributes( thatObject ) ) return false;
			} else {
				if( !Objects.equals( thisObject, thatObject ) ) return false;
			}
		}

		return true;
	}

	@SuppressWarnings( "unchecked" )
	protected <T> T getMetaValue( String name ) {
		// Null attribute names are not allowed.
		if( name == null ) throw new NullPointerException( "Meta value name cannot be null." );

		if( MODIFIED.equals( name ) ) return (T)(isModified() ? Boolean.TRUE : Boolean.FALSE);

		return (T)( metaValues == null ? null : metaValues.get( name ) );
	}

	protected void setMetaValue( String name, Object newValue ) {
		// Null attribute names are not allowed.
		if( name == null ) throw new NullPointerException( "Meta value name cannot be null." );

		// If the old value is equal to the new value no changes are necessary.
		Object oldValue = getMetaValue( name );
		if( Objects.equals( oldValue, newValue ) ) return;

		Transaction.create();
		Transaction.submit( new SetMetaValueOperation( this, name, oldValue, newValue ) );
		Transaction.commit();
	}

	protected void applyMetaValue( String name, Object oldValue, Object newValue ) {
		if( MODIFIED.equals( name ) ) {
			boolean value = (Boolean)newValue;
			if( value == true ) {
				selfModified = true;
			} else {
				selfModified = false;
				modifiedDataValues = null;
				modifiedAttributeCount = 0;
			}
			updateModifiedFlag();
		}
	}

	/**
	 * Clear the modified flag for this node and all data value nodes.
	 */
	void unmodify() {
		if( !modified ) return;

		Transaction.create();

		// Clear the modified flag for this node.
		setMetaValue( MODIFIED, false );

		// Clear the modified flag of any data value nodes.
		if( dataValues != null ) {
			for( Object child : dataValues.values() ) {
				if( child instanceof DataNode ) {
					DataNode childNode = (DataNode)child;
					if( childNode.isModified() ) childNode.unmodify();
				}
			}
		}

		Transaction.commit();
	}

	void updateModifiedFlag() {
		modified = selfModified || modifiedAttributeCount != 0;
	}

	void doSetDataValue( String name, Object oldValue, Object newValue ) {
		// Create the attribute map if necessary.
		if( dataValues == null ) dataValues = new ConcurrentHashMap<String, Object>();

		// Set the attribute value.
		if( newValue == null ) {
			dataValues.remove( name );
			if( oldValue instanceof DataNode ) ( (DataNode)oldValue ).setParent( null );
		} else {
			dataValues.put( name, newValue );
			if( newValue instanceof DataNode ) ( (DataNode)newValue ).setParent( this );
		}

		// Remove the attribute map if necessary.
		if( dataValues.size() == 0 ) dataValues = null;

		// Update the modified attribute value map.
		Object preValue = modifiedDataValues == null ? null : modifiedDataValues.get( name );
		if( preValue == null ) {
			// Only add the value if there is not an existing previous value.
			if( modifiedDataValues == null ) modifiedDataValues = new ConcurrentHashMap<String, Object>();
			modifiedDataValues.put( name, oldValue == null ? NULL : oldValue );
			modifiedAttributeCount++;
		} else if( Objects.equals( preValue == NULL ? null : preValue, newValue ) ) {
			modifiedDataValues.remove( name );
			modifiedAttributeCount--;
			if( modifiedDataValues.size() == 0 ) modifiedDataValues = null;
		}

		updateModifiedFlag();
	}

	void doSetMetaValue( String name, Object oldValue, Object newValue ) {
		// Create the meta value map if necessary.
		if( metaValues == null ) metaValues = new ConcurrentHashMap<String, Object>();

		// Set the value.
		if( newValue == null ) {
			metaValues.remove( name );
			if( oldValue instanceof DataNode ) ( (DataNode)oldValue ).setParent( null );
		} else {
			metaValues.put( name, newValue );
			if( newValue instanceof DataNode ) ( (DataNode)newValue ).setParent( this );
		}

		// Remove the meta value map if necessary.
		if( metaValues.size() == 0 ) metaValues = null;

		// Update the modified meta value map.
		Object preValue = modifiedMetaValues == null ? null : modifiedMetaValues.get( name );
		if( preValue == null ) {
			// Only add the value if there is not an existing previous value.
			if( modifiedMetaValues == null ) modifiedMetaValues = new ConcurrentHashMap<String, Object>();
			modifiedMetaValues.put( name, oldValue == null ? NULL : oldValue );
		} else if( Objects.equals( preValue == NULL ? null : preValue, newValue ) ) {
			modifiedMetaValues.remove( name );
			if( modifiedMetaValues.size() == 0 ) modifiedMetaValues = null;
		}

		applyMetaValue( name, oldValue, newValue );
	}

	/*
	 * Similar logic is found in DataList.listNodeChildModified().
	 */
	void dataNodeModified( boolean modified ) {
		if( modified ) {
			modifiedAttributeCount++;
		} else {
			modifiedAttributeCount--;

			// The reason for the following line is that doUnmodify() is
			// processed by transactions before processing child and parent nodes.
			if( modifiedAttributeCount < 0 ) modifiedAttributeCount = 0;
		}

		updateModifiedFlag();
	}

	void setParent( DataNode parent ) {
		checkForCircularReference( parent );
		this.parent = parent;
	}

	void dispatchEvent( DataEvent event ) {
		switch( event.getType() ) {
			case DATA_CHANGED: {
				fireDataChanged( (DataChangedEvent)event );
				break;
			}
			case META_ATTRIBUTE: {
				fireMetaAttributeChanged( (MetaAttributeEvent)event );
				break;
			}
			case DATA_ATTRIBUTE: {
				fireDataAttributeChanged( (DataAttributeEvent)event );
				break;
			}
		}
	}

	void checkForCircularReference( DataNode node ) {
		DataNode parent = this;
		while( parent != null ) {
			if( node == parent ) throw new CircularReferenceException( "Circular reference detected: " + node );
			parent = parent.getParent();
		}
	}

	private void fireDataChanged( DataChangedEvent event ) {
		for( DataListener listener : this.listeners ) {
			listener.dataChanged( event );
		}
	}

	private void fireDataAttributeChanged( DataAttributeEvent event ) {
		for( DataListener listener : listeners ) {
			listener.dataAttributeChanged( event );
		}
	}

	private void fireMetaAttributeChanged( MetaAttributeEvent event ) {
		for( DataListener listener : listeners ) {
			listener.metaAttributeChanged( event );
		}
	}

}
