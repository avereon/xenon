package com.parallelsymmetry.essence.workarea;

public interface WorkpaneListener {

	void handle( WorkpaneEvent event ) throws WorkpaneVetoException;

}
