package com.xeomar.xenon.testutil;

import com.xeomar.product.ProductEvent;
import com.xeomar.product.ProductEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProgramEventCollector implements ProductEventListener {

	private List<ProductEvent> events = new CopyOnWriteArrayList<>();

	@Override
	public void eventOccurred( ProductEvent event ) {
		events.add( event );
	}

	public List<ProductEvent> getEvents() {
		return events;
	}

}
