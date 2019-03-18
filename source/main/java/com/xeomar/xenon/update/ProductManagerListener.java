package com.xeomar.xenon.update;

import com.xeomar.product.ProductEventListener;

public interface ProductManagerListener extends ProductEventListener {

	void eventOccurred( ProductManagerEvent event );

}
