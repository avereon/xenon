package com.parallelsymmetry.essence.node;

import java.util.EventListener;

public interface NodeListener extends EventListener {

	void eventOccurred( NodeEvent event );

}
