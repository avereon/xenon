package com.avereon.xenon.product;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.product.ProductCard;
import com.avereon.xenon.task.TaskEvent;
import lombok.extern.flogger.Flogger;

@Flogger
public class DownloadRequest implements EventHandler<TaskEvent> {

	private final ProductCard card;

	private final EventHub bus;

	public DownloadRequest( ProductCard card ) {
		this.card = card;
		this.bus = new EventHub();
	}

	public ProductCard getCard() {
		return card;
	}

	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return bus.register( type, handler );
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return bus.unregister( type, handler );
	}

	@Override
	public void handle( TaskEvent event ) {
		bus.dispatch( event );
	}

}
