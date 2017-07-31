package com.xeomar.xenon.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

class TaskWatcher implements TaskListener {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	private List<TaskEvent> events = new CopyOnWriteArrayList<TaskEvent>();

	private Map<TaskEvent.Type, TaskEvent> eventMap = new ConcurrentHashMap<>();

	public List<TaskEvent> getEvents() {
		return new ArrayList<TaskEvent>( events );
	}

	@Override
	public synchronized void handleEvent( TaskEvent event ) {
		events.add( event );
		eventMap.put( event.getType(), event );
		notifyAll();
	}

	public void clearEvent( TaskEvent.Type type ) {
		eventMap.remove( type );
	}

	public void waitForEvent( TaskEvent.Type type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	public void waitForNextEvent( TaskEvent.Type type ) throws InterruptedException, TimeoutException {
		waitForNextEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	public synchronized void waitForEvent( TaskEvent.Type type, long timeout ) throws InterruptedException, TimeoutException {
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
	}

	public synchronized void waitForNextEvent( TaskEvent.Type type, long timeout ) throws InterruptedException, TimeoutException {
		clearEvent( type );
		waitForEvent( type, timeout );
	}

	public synchronized void waitForEventCount( int count, int timout ) throws InterruptedException {
		while( events.size() < count ) {
			wait( timout );
		}
	}

}
