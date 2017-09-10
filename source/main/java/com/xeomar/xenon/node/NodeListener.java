package com.xeomar.xenon.node;

import java.util.EventListener;

public interface NodeListener extends EventListener {

	void nodeEvent( NodeEvent event );

}
