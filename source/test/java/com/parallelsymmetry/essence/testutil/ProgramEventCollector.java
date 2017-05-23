package com.parallelsymmetry.essence.testutil;

import com.parallelsymmetry.essence.ProgramEvent;
import com.parallelsymmetry.essence.ProgramEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProgramEventCollector implements ProgramEventListener {

	private List<ProgramEvent> events = new CopyOnWriteArrayList<>();

	@Override
	public void eventOccurred( ProgramEvent event ) {
		events.add( event );
	}

	public List<ProgramEvent> getEvents() {
		return events;
	}

}
