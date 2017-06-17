package com.parallelsymmetry.essence.node;

import com.parallelsymmetry.essence.transaction.TxnEvent;

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

	private Object oldValue;

	private Object newValue;

	private Node child;

	public NodeEvent( Node node, Type type ) {
		super( node );
		this.type = type;
	}

	public NodeEvent( Node node, Type type, Node child ) {
		this( node, type );
		this.child = child;
	}

	public NodeEvent( Node node, Type type, Object oldValue, Object newValue ) {
		this( node, type );
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Type getType() {
		return type;
	}

	public Node getNode() {
		return node;
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

}
