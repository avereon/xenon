package com.parallelsymmetry.essence.work;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workarea {

	private String name;

	private Set<PropertyChangeListener> listeners;

	public Workarea( String name ) {
		this.name = name;
		listeners = new CopyOnWriteArraySet<>();
	}

	public String getName() {
		return name;
	}

	public void setName( String newName ) {
		String oldName = this.name;
		this.name = newName;
		firePropertyChange( "name", oldName, newName );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener ) {
		listeners.add( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener ) {
		listeners.remove( listener );
	}

	private void firePropertyChange( String property, Object oldValue, Object newValue ) {
		PropertyChangeEvent event = new PropertyChangeEvent( this, property, oldValue, newValue );
		for( PropertyChangeListener listener : listeners ) {
			listener.propertyChange( event );
		}
	}

}
