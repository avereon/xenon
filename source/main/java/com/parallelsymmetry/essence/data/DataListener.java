package com.parallelsymmetry.essence.data;

import com.parallelsymmetry.essence.data.event.DataAttributeEvent;
import com.parallelsymmetry.essence.data.event.DataChangedEvent;
import com.parallelsymmetry.essence.data.event.DataChildEvent;
import com.parallelsymmetry.essence.data.event.MetaAttributeEvent;

public interface DataListener {

	void dataChanged( DataChangedEvent event );

	void metaAttributeChanged( MetaAttributeEvent event );

	void dataAttributeChanged( DataAttributeEvent event );

	void childInserted( DataChildEvent event );

	void childRemoved( DataChildEvent event );

}
