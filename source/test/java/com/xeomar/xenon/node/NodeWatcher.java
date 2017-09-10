package com.xeomar.xenon.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeWatcher implements NodeListener {

	private List<NodeEvent> events = new CopyOnWriteArrayList<>();

	@Override
	public void nodeEvent( NodeEvent event ) {
		events.add( event );
	}

	public List<NodeEvent> getEvents() {
		return new ArrayList<>( events );
	}

	public void reset() {
		events.clear();
	}
}
