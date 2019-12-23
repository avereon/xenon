package com.avereon.xenon;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class ProgramWatcher implements ProductEventListener {

	public static final long DEFAULT_WAIT_TIMEOUT = 10000;

	private Queue<ProductEventOld> events = new ConcurrentLinkedQueue<>();

	@Override
	public synchronized void handleEvent( ProductEventOld event ) {
		events.offer( event );
		notifyAll();
	}

	public void waitForEvent( Class<? extends ProductEventOld> type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	public void waitForNextEvent( Class<? extends ProductEventOld> type ) throws InterruptedException, TimeoutException {
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
	public synchronized void waitForEvent( Class<? extends ProductEventOld> type, long timeout ) throws InterruptedException, TimeoutException {
		boolean shouldWait = timeout > 0;
		long start = System.currentTimeMillis();
		long duration = 0;

		while( shouldWait && findNext( type ) == null ) {
			wait( timeout - duration );
			duration = System.currentTimeMillis() - start;
			shouldWait = duration < timeout;
		}
		duration = System.currentTimeMillis() - start;

		if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + type.getName() );
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
	public synchronized void waitForNextEvent( Class<? extends ProductEventOld> type, long timeout ) throws InterruptedException, TimeoutException {
		events.remove( type );
		waitForEvent( type, timeout );
	}

	private ProductEventOld findNext( Class<? extends ProductEventOld> type ) {
		ProductEventOld event;
		while( (event = events.poll()) != null ) {
			if( event.getClass().isAssignableFrom( type ) ) return event;
		}
		return null;
	}

}
