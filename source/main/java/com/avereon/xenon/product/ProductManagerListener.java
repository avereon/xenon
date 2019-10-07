package com.avereon.xenon.product;

import com.avereon.product.ProductEventListener;

public interface ProductManagerListener extends ProductEventListener {

	void eventOccurred( ProductManagerEvent event );

}
