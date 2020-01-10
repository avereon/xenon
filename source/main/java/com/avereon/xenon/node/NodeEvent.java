package com.avereon.xenon.node;

import com.avereon.event.EventType;
import com.avereon.xenon.transaction.TxnEvent;

import java.util.Objects;

public class NodeEvent extends TxnEvent {

	public static final EventType<NodeEvent> VALUE_CHANGED = new EventType<>( "VALUE_CHANGED" );

	//public static final EventType<NodeEvent> CHILD_ADDED = new EventType<>( "CHILD_ADDED" );

	//public static final EventType<NodeEvent> CHILD_REMOVED = new EventType<>( "CHILD_REMOVED" );

	public static final EventType<NodeEvent> MODIFIED = new EventType<>( "MODIFIED" );

	public static final EventType<NodeEvent> UNMODIFIED = new EventType<>( "UNMODIFIED" );

	public static final EventType<NodeEvent> NODE_CHANGED = new EventType<>( "NODE_CHANGED" );

	// TODO Should there be PARENT_CHANGED events???

	private Node node;

	private String key;

	private Object oldValue;

	private Object newValue;

	private Node child;

	public NodeEvent( Node node, EventType<? extends NodeEvent> type ) {
		this( node, null, type );
	}

	public NodeEvent( Node node, Node child, EventType<? extends NodeEvent> type ) {
		this( node, child, type, null, null, null );
	}

	public NodeEvent( Node node, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue ) {
		this( node, null, type, key, oldValue, newValue );
	}

	public NodeEvent( Node node, Node child, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue ) {
		super( child == null ? node : child, type, node );
		this.node = node;
		this.child = child;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Node getNode() {
		return node;
	}

	public String getKey() {
		return key;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	public Node getChild() {
		return child;
	}

	@SuppressWarnings( "unchecked" )
	public EventType<? extends NodeEvent> getEventType() {
		return (EventType<? extends NodeEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( getClass().getSimpleName() );
		builder.append( "[ " );
		builder.append( "node=" );
		builder.append( node );
		if( child != null ) {
			builder.append( ", child=" );
			builder.append( child );
		}
		builder.append( ", type=" );
		builder.append( getEventType() );
		if( key != null ) {
			builder.append( ", key=" );
			builder.append( key );
			builder.append( ", oldValue=" );
			builder.append( oldValue );
			builder.append( ", newValue=" );
			builder.append( newValue );
		}
		builder.append( " ]" );

		return builder.toString();
	}

	@Override
	public int hashCode() {
		int code = 0;

		if( node != null ) code |= node.hashCode();
		if( getEventType() != null ) code |= getEventType().hashCode();
		if( key != null ) code |= key.hashCode();
		//		if( oldValue != null ) code |= oldValue.hashCode();
		//		if( newValue != null ) code |= newValue.hashCode();
		if( child != null ) code |= child.hashCode();

		return code;
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof NodeEvent) ) return false;

		NodeEvent that = (NodeEvent)object;
		if( !Objects.equals( this.node, that.node ) ) return false;
		if( !Objects.equals( this.getEventType(), that.getEventType() ) ) return false;
		if( !Objects.equals( this.key, that.key ) ) return false;
		//		if( !Objects.equals( this.oldValue, that.oldValue ) ) return false;
		//		if( !Objects.equals( this.newValue, that.newValue ) ) return false;
		return Objects.equals( this.child, that.child );
	}

}
