package com.avereon.xenon.tool.guide;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GuideContext {

	private final GuidedTool tool;

	private final EventHub eventHub;

	private final ObservableList<Guide> guides;

	private Guide currentGuide;

	public GuideContext( GuidedTool tool ) {
		this.tool = tool;
		this.eventHub = new EventHub();
		this.guides = FXCollections.observableArrayList();
	}

	public GuidedTool getTool() {
		return tool;
	}

	public ObservableList<Guide> getGuides() {
		return guides;
	}

	public Guide getCurrentGuide() {
		if( currentGuide == null && !guides.isEmpty() ) currentGuide = guides.get( 0 );
		return currentGuide;
	}

	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return eventHub.register( type, handler );
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return eventHub.unregister( type, handler );
	}

	public Event dispatch( Event event ) {
		return eventHub.dispatch( event );
	}

}
