package com.parallelsymmetry.essence.node;

import com.parallelsymmetry.essence.transaction.TxnEvent;

import java.util.Objects;

public class NodeEvent extends TxnEvent {

	public enum Type {
		VALUE_CHANGED,
		FLAG_CHANGED,
		NODE_CHANGED,
		CHILD_ADDED,
		CHILD_REMOVED
	}

	private Type type;

	private Node node;

	private String key;

	private Object oldValue;

	private Object newValue;

	private Node child;

	public NodeEvent( Node node, Type type ) {
		super( node );
		this.node = node;
		this.type = type;
	}

	public NodeEvent( Node node, Type type, Node child ) {
		this( node, type );
		this.child = child;
	}

	public NodeEvent( Node node, Type type, String key, Object oldValue, Object newValue ) {
		this( node, type );
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Type getType() {
		return type;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(getClass().getSimpleName());
		builder.append( "[");
		builder.append( "type=" );
		builder.append( type );
		if( key != null ) {
			builder.append( ",key=" );
			builder.append( key );
		}
		builder.append( "]");

		return builder.toString();
	}

	@Override
	public int hashCode() {
		int code = 0;

		if( node != null ) code |= node.hashCode();
		if( type != null ) code |= type.hashCode();
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
		if( !Objects.equals( this.type, that.type )) return false;
		if( !Objects.equals( this.key, that.key )) return false;
//		if( !Objects.equals( this.oldValue, that.oldValue ) ) return false;
//		if( !Objects.equals( this.newValue, that.newValue ) ) return false;
		if( !Objects.equals( this.child, that.child ) ) return false;

		return true;
	}

}
