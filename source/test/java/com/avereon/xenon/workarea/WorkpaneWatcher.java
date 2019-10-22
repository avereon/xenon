package com.avereon.xenon.workarea;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class WorkpaneWatcher implements WorkpaneListener {

	private static final long DEFAULT_WAIT_TIMEOUT = 10000;

	private Queue<WorkpaneEvent> events = new ConcurrentLinkedQueue<>();

	@Override
	public synchronized void handle( WorkpaneEvent event ) {
		events.offer( event );
		notifyAll();
	}

	public List<WorkpaneEvent> getEvents() {
		return new ArrayList<>( events );
	}

	public void waitForEvent( WorkpaneEvent.Type type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	@SuppressWarnings( "unused" )
	public void waitForNextEvent( WorkpaneEvent.Type type ) throws InterruptedException, TimeoutException {
		waitForNextEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	/**
	 * Wait for an event of a specific type to occur. If the event has already occurred this method will return immediately. If the event has not already occurred then this method waits until the next event occurs, or the specified timeout,
	 * whichever comes first.
	 *
	 * @param type The event type to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForEvent( WorkpaneEvent.Type type, long timeout ) throws InterruptedException, TimeoutException {
		if( timeout <=0 ) return;

		long duration = 0;
		boolean shouldWait = true;
		long start = System.currentTimeMillis();

		while( shouldWait && findNext( type ) == null ) {
			wait( timeout - duration );
			duration = System.currentTimeMillis() - start;
			shouldWait = duration < timeout;
		}

		duration = System.currentTimeMillis() - start;
		if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + type );
	}

	private WorkpaneEvent findNext( WorkpaneEvent.Type type ) {
		WorkpaneEvent event;
		while( (event = events.poll()) != null ) {
			if( event.getType() == type ) return event;
		}
		return null;
	}

	/**
	 * Wait for the next event of a specific type to occur. This method always waits until the next event occurs, or the specified timeout, whichever comes first.
	 *
	 * @param type The event type to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	@SuppressWarnings( "SameParameterValue" )
	private synchronized void waitForNextEvent( WorkpaneEvent.Type type, long timeout ) throws InterruptedException, TimeoutException {
		events.clear();
		waitForEvent( type, timeout );
	}

}
