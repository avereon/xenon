package com.parallelsymmetry.essence.data;

import com.parallelsymmetry.utility.data.*;
import com.parallelsymmetry.utility.log.Log;

import java.util.ArrayList;
import java.util.List;

public class DataEventWatcher implements DataListener {

	private String name;

	private List<DataEvent> events = new ArrayList<DataEvent>();

	private List<DataChangedEvent> dataChangedEvents = new ArrayList<DataChangedEvent>();

	private List<MetaAttributeEvent> metaAttributeEvents = new ArrayList<MetaAttributeEvent>();

	private List<DataAttributeEvent> dataAttributeEvents = new ArrayList<DataAttributeEvent>();

	private List<DataChildEvent> childInsertedEvents = new ArrayList<DataChildEvent>();

	private List<DataChildEvent> childRemovedEvents = new ArrayList<DataChildEvent>();

	public DataEventWatcher() {
		this( null );
	}

	public DataEventWatcher( String name ) {
		this.name = ( name == null ? "" : name );
	}

	@Override
	public void dataChanged( DataChangedEvent event ) {
		Log.write( Log.TRACE, name, ": Data change event received." );
		dataChangedEvents.add( event );
		events.add( event );
	}

	@Override
	public void metaAttributeChanged( MetaAttributeEvent event ) {
		Log.write( Log.TRACE, name, ": Meta attribute change event received." );
		metaAttributeEvents.add( event );
		events.add( event );
	}

	@Override
	public void dataAttributeChanged( DataAttributeEvent event ) {
		Log.write( Log.TRACE, name, ": Data attribute change event received." );
		dataAttributeEvents.add( event );
		events.add( event );
	}

	@Override
	public void childInserted( DataChildEvent event ) {
		Log.write( Log.TRACE, name, ": Child inserted event received." );
		childInsertedEvents.add( event );
		events.add( event );
	}

	@Override
	public void childRemoved( DataChildEvent event ) {
		Log.write( Log.TRACE, name, ": Child removed event received." );
		childRemovedEvents.add( event );
		events.add( event );
	}

	public List<DataEvent> getEvents() {
		return events;
	}

	public List<DataChangedEvent> getDataChangedEvents() {
		return dataChangedEvents;
	}

	public List<MetaAttributeEvent> getMetaAttributeEvents() {
		return metaAttributeEvents;
	}

	public List<DataAttributeEvent> getDataAttributeEvents() {
		return dataAttributeEvents;
	}

	public List<DataChildEvent> getChildInsertedEvents() {
		return childInsertedEvents;
	}

	public List<DataChildEvent> getChildRemovedEvents() {
		return childRemovedEvents;
	}

	public void reset() {
		dataChangedEvents.clear();
		metaAttributeEvents.clear();
		dataAttributeEvents.clear();
		childInsertedEvents.clear();
		childRemovedEvents.clear();
		events.clear();
	}

}
