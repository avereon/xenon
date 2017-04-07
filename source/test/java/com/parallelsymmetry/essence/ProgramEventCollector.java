package com.parallelsymmetry.essence;

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
