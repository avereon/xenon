package com.avereon.xenon.transaction;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class TxnEvent extends Event {

	public static final EventType<TxnEvent> COMMIT_BEGIN = new EventType<>( "COMMIT_BEGIN" );

	public static final EventType<TxnEvent> COMMIT_SUCCESS = new EventType<>( "COMMIT_SUCCESS" );

	public static final EventType<TxnEvent> COMMIT_FAIL = new EventType<>( "COMMIT_FAIL" );

	public static final EventType<TxnEvent> COMMIT_END = new EventType<>( "COMMIT_END" );

	private TxnEventTarget target;

	/**
	 * Create a TxnEvent where the source and target are the same object. This is
	 * a common pattern where the eventual target of the event is the same object
	 * that is creating it.
	 *
	 * @param sourceTarget The event source/target
	 */
	public TxnEvent( TxnEventTarget sourceTarget, EventType<? extends TxnEvent> type ) {
		this( sourceTarget, type, sourceTarget );
	}

	/**
	 * Create a TxnEvent where the source and target are different objects. This
	 * is also a common pattern where the eventual target of the event is a
	 * different object, like the parent of the object.
	 *
	 * @param source The event source
	 * @param target The event target
	 */
	public TxnEvent( Object source, EventType<? extends TxnEvent> type, TxnEventTarget target ) {
		super( source, type );
		this.target = target;
	}

	/**
	 * Get the object that is the target of the event.
	 *
	 * @return The target object
	 */
	public TxnEventTarget getTarget() {
		return target;
	}

}
