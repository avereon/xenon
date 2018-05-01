package com.xeomar.xenon.update;

import com.xeomar.product.ProductEventListener;

public interface UpdateManagerListener extends ProductEventListener {

	void eventOccurred( UpdateManagerEvent event );

}
