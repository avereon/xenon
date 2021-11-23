package com.avereon.xenon.task;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.xenon.task.TaskManagerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

class TaskWatcher implements EventHandler<TaskManagerEvent> {

	private static final long DEFAULT_WAIT_TIMEOUT = 2000;

	private List<TaskManagerEvent> events = new CopyOnWriteArrayList<>();

	private Map<EventType<? extends Event>, TaskManagerEvent> eventMap = new ConcurrentHashMap<>();

	public List<? extends TaskManagerEvent> getEvents() {
		return new ArrayList<>( events );
	}

	@Override
	public synchronized void handle( TaskManagerEvent event ) {
		events.add( event );
		eventMap.put( event.getEventType(), event );
		notifyAll();
	}

	private void clearEvent( EventType<? extends Event> type ) {
		eventMap.remove( type );
	}

	public void waitForEvent( EventType<? extends Event> type ) throws InterruptedException, TimeoutException {
		waitForEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	@SuppressWarnings( "unused" )
	public void waitForNextEvent( EventType<? extends Event> type ) throws InterruptedException, TimeoutException {
		waitForNextEvent( type, DEFAULT_WAIT_TIMEOUT );
	}

	public synchronized void waitForEvent( EventType<? extends Event> type, long timeout ) throws InterruptedException, TimeoutException {
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

	@SuppressWarnings( "SameParameterValue" )
	private synchronized void waitForNextEvent( EventType<? extends Event> type, long timeout ) throws InterruptedException, TimeoutException {
		clearEvent( type );
		waitForEvent( type, timeout );
	}

	@SuppressWarnings( "unused" )
	public synchronized void waitForEventCount( int count, int timout ) throws InterruptedException {
		while( events.size() < count ) {
			wait( timout );
		}
	}

}
