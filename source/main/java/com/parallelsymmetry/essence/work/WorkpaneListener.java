package com.parallelsymmetry.essence.work;

public interface WorkpaneListener {

	void paneChanged( WorkpaneEvent event );

	void viewAdded( WorkpaneEvent event );

	void viewRemoved( WorkpaneEvent event );

	void viewActivated( WorkpaneEvent event );

	void viewDeactivated( WorkpaneEvent event );

	void viewWillSplit( WorkpaneEvent event ) throws WorkpaneVetoException;

	void viewSplit( WorkpaneEvent event );

	void viewWillMerge( WorkpaneEvent event ) throws WorkpaneVetoException;

	void viewMerged( WorkpaneEvent event );

	void toolAdded( WorkpaneEvent event );

	void toolRemoved( WorkpaneEvent event );

	void toolActivated( WorkpaneEvent event );

	void toolDeactivated( WorkpaneEvent event );

}
