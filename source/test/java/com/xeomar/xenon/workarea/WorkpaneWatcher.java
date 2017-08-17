package com.xeomar.xenon.workarea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public class WorkpaneWatcher implements WorkpaneListener {

	private static final long DEFAULT_WAIT_TIMEOUT = 5000;

	private List<WorkpaneEvent> eventList = new CopyOnWriteArrayList<WorkpaneEvent>();

	private Map<WorkpaneEvent.Type, WorkpaneEvent> eventMap = new ConcurrentHashMap<>();

	@Override
	public synchronized void handle( WorkpaneEvent event ) throws WorkpaneVetoException {
		eventList.add( event );
		eventMap.put( event.getType(), event );
		notifyAll();
	}

	public List<WorkpaneEvent> getEvents() {
		return new ArrayList<WorkpaneEvent>( eventList );
	}

	protected void waitForEvent( WorkpaneEvent.Type type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	protected void waitForNextEvent( WorkpaneEvent.Type type ) throws InterruptedException, TimeoutException {
		waitForNextEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	/**
	 * Wait for an event of a specific type to occur. If the event has already
	 * occurred this method will return immediately. If the event has not
	 * already occurred then this method waits until the next event occurs, or
	 * the specified timeout, whichever comes first.
	 *
	 * @param type The event type to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForEvent( WorkpaneEvent.Type type, long timeout ) throws InterruptedException, TimeoutException {
		boolean shouldWait = timeout > 0;
		long start = System.currentTimeMillis();
		long duration = 0;

		while( shouldWait && eventMap.get( type ) == null ) {
			wait( timeout - duration );
			duration = System.currentTimeMillis() - start;
			shouldWait = duration < timeout;
		}
		duration = System.currentTimeMillis() - start;

		if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + type );

		clearEvent( type );
	}

	public synchronized void clearEvent( WorkpaneEvent.Type type ) {
		eventMap.remove( type );
	}

	/**
	 * Wait for the next event of a specific type to occur. This method always
	 * waits until the next event occurs, or the specified timeout, whichever
	 * comes first.
	 *
	 * @param type The event type to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForNextEvent( WorkpaneEvent.Type type, long timeout ) throws InterruptedException, TimeoutException {
		clearEvent( type );
		waitForEvent( type, timeout );
	}

}
