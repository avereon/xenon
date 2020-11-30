package com.avereon.xenon.undo;

import com.avereon.data.Node;
import com.avereon.data.NodeEvent;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import java.util.Objects;

public class NodeChange {

	public static final String CAPTURE_UNDO_CHANGES = "capture-undo-changes";

	private final Node node;

	private final String key;

	private final Object oldValue;

	private final Object newValue;

	public NodeChange( Node node, String key, Object oldValue, Object newValue ) {
		this.node = node;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public NodeChange invert() {
		return new NodeChange( node, key, newValue, oldValue );
	}

	public void apply() {
		node.setValue( key, newValue );
	}

	public static EventStream<NodeChange> events( Node node ) {
		EventSource<NodeChange> source = new EventSource<>();
		node.register( NodeEvent.VALUE_CHANGED, e -> {
			Node eventNode = e.getNode();
			String eventKey = e.getKey();
			boolean isModifying = eventNode.isModifyingKey( eventKey );
			boolean isCaptureUndoChanges = node.getValue( CAPTURE_UNDO_CHANGES, true );
			if( isModifying && isCaptureUndoChanges ) source.push( new NodeChange( eventNode, eventKey, e.getOldValue(), e.getNewValue() ) );
		} );
		return source;
	}

	@Override
	public boolean equals( Object other ) {
		if( this == other ) return true;
		if( other == null || getClass() != other.getClass() ) return false;
		NodeChange that = (NodeChange)other;
		return node.equals( that.node ) && key.equals( that.key ) && Objects.equals( oldValue, that.oldValue ) && Objects.equals( newValue, that.newValue );
	}

	@Override
	public int hashCode() {
		return Objects.hash( node, key, oldValue, newValue );
	}
}
