package com.avereon.xenon.update;

import com.avereon.product.ProductEventListener;

public interface ProductManagerListener extends ProductEventListener {

	void eventOccurred( ProductManagerEvent event );

}
