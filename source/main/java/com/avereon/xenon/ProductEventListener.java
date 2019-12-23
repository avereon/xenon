package com.avereon.xenon;

import java.util.EventListener;

/**
 * @deprecated In favor of {@link com.avereon.event.EventHandler}
 * @param <T>
 */
@Deprecated
public interface ProductEventListener<T extends ProductEventOld> extends EventListener {

	void handleEvent( T event );

}
