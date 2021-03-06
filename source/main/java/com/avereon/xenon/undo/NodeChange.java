package com.avereon.xenon.undo;

import com.avereon.data.Node;
import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CustomLog
public class NodeChange {

	public static final String CAPTURE_UNDO_CHANGES = NodeChange.class.getName() + ":capture-undo-changes";

	//public static final String PUBLISH_UNDO_CHANGES = NodeChange.class.getName() + ":publish-undo-changes";

	public static final String CAPTURE_REDO_CHANGES = "capture-redo-changes";

	public static final boolean DEFAULT_CAPTURE_UNDO_CHANGES = false;

	private final Node node;

	private final String key;

	private final Object oldValue;

	private final Object newValue;

	private final boolean redo;

	private final List<NodeChange> changes;

	public NodeChange( Node node, String key, Object oldValue, Object newValue ) {
		this( node, key, oldValue, newValue, false );
	}

	public NodeChange( Node node, String key, Object oldValue, Object newValue, boolean isRedo ) {
		this.node = node;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.redo = isRedo;
		this.changes = List.of( this );
	}

	NodeChange( List<NodeChange> changes ) {
		this.node = null;
		this.key = null;
		this.oldValue = null;
		this.newValue = null;
		this.redo = false;
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

	public boolean isRedo() {
		return redo;
	}

	List<NodeChange> getChanges() {
		return changes;
	}

	public String toString() {
		return node + " " + key + "=" + oldValue + " -> " + newValue;
	}

	@Override
	public boolean equals( Object other ) {
		if( this == other ) return true;
		if( other == null || getClass() != other.getClass() ) return false;

		// FIXME Should only have to compare the change lists

		NodeChange that = (NodeChange)other;
		//if( this.node != null ) {
			return Objects.equals( this.node, that.node ) && Objects.equals( this.key, that.key ) && Objects.equals( oldValue, that.oldValue ) && Objects.equals( newValue, that.newValue );
		//}

		//return true;
		//return Objects.equals( this.changes, that.changes );
	}

	@Override
	public int hashCode() {
		if( this.node != null ) return Objects.hash( node, key, oldValue, newValue );
		return super.hashCode();
	}

}
