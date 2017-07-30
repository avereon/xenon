package com.xeomar.xenon.workarea;

public interface WorkpaneListener {

	void handle( WorkpaneEvent event ) throws WorkpaneVetoException;

}
