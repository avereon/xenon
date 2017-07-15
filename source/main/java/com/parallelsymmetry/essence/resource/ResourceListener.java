package com.parallelsymmetry.essence.resource;

public interface ResourceListener {

	void resourceOpened( ResourceEvent event );

	void resourceLoaded( ResourceEvent event );

	void resourceRefreshed( ResourceEvent event );

	void resourceModified( ResourceEvent event );

	void resourceUnmodified( ResourceEvent event );

	void resourceSaved( ResourceEvent event );

	void resourceClosed( ResourceEvent event );

}
