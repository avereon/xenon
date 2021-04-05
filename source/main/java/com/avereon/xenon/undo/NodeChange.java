package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeChange {

	public static final String CAPTURE_UNDO_CHANGES = "capture-undo-changes";

	public static final boolean DEFAULT_CAPTURE_UNDO_CHANGES = false;

	private static final System.Logger log = Log.get();

	private final Node node;

	private final String key;

	private final Object oldValue;

	private final Object newValue;

	private final List<NodeChange> changes;

	public NodeChange( Node node, String key, Object oldValue, Object newValue ) {
		this.node = node;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.changes = List.of( this );
	}

	NodeChange( List<NodeChange> changes ) {
		this.node = null;
		this.key = null;
		this.oldValue = null;
		this.newValue = null;
		this.changes = new ArrayList<>( changes );
	}

	Node getNode() {
		return node;
	}

	String getKey() {
		return key;
	}

	Object getOldValue() {
		return oldValue;
	}

	Object getNewValue() {
		return newValue;
	}

	List<NodeChange> getChanges() {
		return changes;
	}

	@Override
	public boolean equals( Object other ) {
		if( this == other ) return true;
		if( other == null || getClass() != other.getClass() ) return false;
		NodeChange that = (NodeChange)other;
		if( this.node != null ) {
			return Objects.equals( this.node, that.node ) && Objects.equals( this.key, that.key ) && Objects.equals( oldValue, that.oldValue ) && Objects.equals( newValue, that.newValue );
		}
		//return Objects.equals( this.changes, that.changes );
		return true;
	}

	@Override
	public int hashCode() {
		if( this.node != null ) return Objects.hash( node, key, oldValue, newValue );
		return super.hashCode();
	}

}
