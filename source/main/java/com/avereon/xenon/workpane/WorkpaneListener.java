package com.avereon.xenon.workpane;

public interface WorkpaneListener {

	void handle( WorkpaneEvent event ) throws WorkpaneVetoException;

}
