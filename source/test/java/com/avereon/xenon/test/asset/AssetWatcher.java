package com.avereon.xenon.test.asset;

import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.xenon.asset.AssetEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class AssetWatcher implements EventHandler<AssetEvent> {

	private static final long DEFAULT_WAIT_TIMEOUT = 2500;

	private Map<EventType<? extends AssetEvent>, AssetEvent> events = new ConcurrentHashMap<>();

	@Override
	public synchronized void handle( AssetEvent event ) {
		events.put( event.getEventType(), event );
		notifyAll();
	}

	public void waitForEvent( EventType<AssetEvent> type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	/**
	 * Wait for an event of a specific class to occur. If the event has already
	 * occurred this method will return immediately. If the event has not
	 * already occurred then this method waits until the next event occurs, or
	 * the specified timeout, whichever comes first.
	 *
	 * @param type The event class to wait for
	 * @param timeout How long, in milliseconds, to wait for the event
	 * @throws InterruptedException If the timeout is exceeded
	 */
	public synchronized void waitForEvent( EventType<AssetEvent> type, long timeout ) throws InterruptedException, TimeoutException {
		boolean shouldWait = timeout > 0;
		long start = System.currentTimeMillis();
		long duration = 0;

		while( shouldWait && events.get( type ) == null ) {
			wait( timeout - duration );
			duration = System.currentTimeMillis() - start;
			shouldWait = duration < timeout;
		}
		duration = System.currentTimeMillis() - start;

		if( duration >= timeout ) throw new TimeoutException( "Timeout waiting for event " + type.getName() );
	}

}
