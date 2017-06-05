package com.parallelsymmetry.essence.data;

public interface DataListener {

	void dataChanged( DataChangedEvent event );

	void metaAttributeChanged( MetaAttributeEvent event );

	void dataAttributeChanged( DataAttributeEvent event );

	void childInserted( DataChildEvent event );

	void childRemoved( DataChildEvent event );

}
