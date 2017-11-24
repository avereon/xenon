package com.xeomar.xenon;

import com.xeomar.product.ProductEventListener;

public interface UpdateManagerListener extends ProductEventListener {

	void eventOccurred( UpdateManagerEvent event );

}
