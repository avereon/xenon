package com.parallelsymmetry.essence.data;

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
