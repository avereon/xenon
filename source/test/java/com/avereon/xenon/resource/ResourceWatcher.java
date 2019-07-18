package com.avereon.xenon.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class ResourceWatcher implements ResourceListener {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	private Map<Class<? extends ResourceEvent>, ResourceEvent> events = new ConcurrentHashMap<>();

	@Override
	public synchronized void eventOccurred( ResourceEvent event ) {
		events.put( event.getClass(), event );
		notifyAll();
	}

	public void waitForEvent( Class<? extends ResourceEvent> clazz ) throws InterruptedException, TimeoutException {
		waitForEvent( clazz, DEFAULT_WAIT_TIMEOUT );
	}

	public void waitForNextEvent( Class<? extends ResourceEvent> clazz ) throws InterruptedException, TimeoutException {
		waitForNextEvent( clazz, DEFAULT_WAIT_TIMEOUT );
	}

	/**
	 * Wait for an event of a specific class to occur. If the event has already
	 * occurred this method will return immediately. If the event has not
	 * already occurred then this method waits until the next event occurs, or
	 * the specified timeout, whichever comes first.
	 *
	 * @param clazz The event class to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForEvent( Class<? extends ResourceEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
		boolean shouldWait = timeout > 0;
		long start = System.currentTimeMillis();
		long duration = 0;

		while( shouldWait && events.get( clazz ) == null ) {
			wait( timeout - duration );
			duration = System.currentTimeMillis() - start;
			shouldWait = duration < timeout;
		}
		duration = System.currentTimeMillis() - start;

		if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + clazz.getName() );
	}

	/**
	 * Wait for the next event of a specific class to occur. This method always
	 * waits until the next event occurs, or the specified timeout, whichever
	 * comes first.
	 *
	 * @param clazz The event class to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForNextEvent( Class<? extends ResourceEvent> clazz, long timeout ) throws InterruptedException, TimeoutException {
		events.remove( clazz );
		waitForEvent( clazz, timeout );
	}

}
