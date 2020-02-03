package com.avereon.xenon.product;

import com.avereon.event.Event;
import com.avereon.event.EventBus;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.product.ProductCard;
import com.avereon.util.Log;
import com.avereon.xenon.task.TaskEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class DownloadRequest implements EventHandler<TaskEvent> {

	private static final Logger log = Log.get( MethodHandles.lookup().lookupClass() );

	private ProductCard card;

	private EventBus bus;

	public DownloadRequest( ProductCard card ) {
		this.card = card;
		this.bus = new EventBus();
	}

	public ProductCard getCard() {
		return card;
	}

	public <T extends Event> EventBus register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return bus.register( type, handler );
	}

	public <T extends Event> EventBus unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return bus.unregister( type, handler );
	}

	@Override
	public void handle( TaskEvent event ) {
		bus.dispatch( event );
	}

}
