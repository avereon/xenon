package com.parallelsymmetry.essence.data;

import com.parallelsymmetry.essence.data.event.DataAttributeEvent;
import com.parallelsymmetry.essence.data.event.DataChangedEvent;
import com.parallelsymmetry.essence.data.event.DataChildEvent;
import com.parallelsymmetry.essence.data.event.MetaAttributeEvent;

public class DataAdapter implements DataListener {

	@Override
	public void dataChanged( DataChangedEvent event ) {}

	@Override
	public void dataAttributeChanged( DataAttributeEvent event ) {}

	@Override
	public void metaAttributeChanged( MetaAttributeEvent event ) {}

	@Override
	public void childInserted( DataChildEvent event ) {}

	@Override
	public void childRemoved( DataChildEvent event ) {}

}
