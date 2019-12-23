package com.avereon.xenon.testutil;

import com.avereon.xenon.ProductEventOld;
import com.avereon.xenon.ProductEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings( "unused" )
public class ProgramEventCollector implements ProductEventListener {

	private List<ProductEventOld> events = new CopyOnWriteArrayList<>();

	@Override
	public void handleEvent( ProductEventOld event ) {
		events.add( event );
	}

	@SuppressWarnings( "unused" )
	public List<ProductEventOld> getEvents() {
		return events;
	}

}
