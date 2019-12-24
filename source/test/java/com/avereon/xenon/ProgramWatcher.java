package com.avereon.xenon;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class ProgramWatcher implements EventHandler<Event> {

	public static final long DEFAULT_WAIT_TIMEOUT = 10000;

	private Queue<Event> events = new ConcurrentLinkedQueue<>();

	@Override
	public synchronized void handle( Event event ) {
		events.offer( event );
		notifyAll();
	}

	public void waitForEvent( EventType<ProgramEvent> type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	public void waitForNextEvent( EventType<ProgramEvent> type ) throws InterruptedException, TimeoutException {
		waitForNextEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	/**
	 * Wait for an event of a specific class to occur. If the event has already
	 * occurred this method will return immediately. If the event has not
	 * already occurred then this method waits until the next event occurs, or
	 * the specified timeout, whichever comes first.
	 *
	 * @param type The event type to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForEvent( EventType<ProgramEvent> type, long timeout ) throws InterruptedException, TimeoutException {
		boolean shouldWait = timeout > 0;
		long start = System.currentTimeMillis();
		long duration = 0;

		while( shouldWait && findNext( type ) == null ) {
			wait( timeout - duration );
			duration = System.currentTimeMillis() - start;
			shouldWait = duration < timeout;
		}
		duration = System.currentTimeMillis() - start;

		if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + type );
	}

	/**
	 * Wait for the next event of a specific class to occur. This method always
	 * waits until the next event occurs, or the specified timeout, whichever
	 * comes first.
	 *
	 * @param type The event class to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForNextEvent( EventType<ProgramEvent> type, long timeout ) throws InterruptedException, TimeoutException {
		findNext( type );
		waitForEvent( type, timeout );
	}

	private Event findNext( EventType<ProgramEvent> type ) {
		Event event;
		while( (event = events.poll()) != null ) {
			if( event.getEventType() ==  type ) return event;
		}
		return null;
	}

}
