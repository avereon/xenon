package com.avereon.xenon.product;

import com.avereon.event.Event;
import com.avereon.event.EventHub;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.product.ProductCard;
import com.avereon.util.Log;
import com.avereon.xenon.task.TaskEvent;
import java.lang.System.Logger;

public class DownloadRequest implements EventHandler<TaskEvent> {

	private static final Logger log = Log.get();

	private ProductCard card;

	private EventHub bus;

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
