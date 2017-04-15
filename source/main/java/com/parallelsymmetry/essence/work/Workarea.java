package com.parallelsymmetry.essence.work;

import org.apache.commons.configuration2.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workarea {

	private String id;

	private String name;

	private boolean active;

	private Workspace workspace;

	private Set<PropertyChangeListener> listeners;

	private Configuration configuration;

	public Workarea() {
		listeners = new CopyOnWriteArraySet<>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName( String newName ) {
		String oldName = name;
		name = newName;
		firePropertyChange( "name", oldName, newName );
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace( Workspace workspace ) {
		Workspace oldWorkspace = this.workspace;

		this.workspace = workspace;

		if( this.workspace != null ) {
			configuration.setProperty( "workspaceId", this.workspace.getId() );
		} else {
			configuration.clearProperty( "workspaceId" );
		}

		firePropertyChange( "workspace", oldWorkspace, this.workspace );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener ) {
		listeners.add( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener ) {
		listeners.remove( listener );
	}

	public void setConfiguration( Configuration configuration ) {
		if( this.configuration != null ) return;

		this.configuration = configuration;

		id = configuration.getString( "id" );
		setName( configuration.getString( "name" ) );
		setActive( configuration.getBoolean( "active" ) );
	}

	private void firePropertyChange( String property, Object oldValue, Object newValue ) {
		PropertyChangeEvent event = new PropertyChangeEvent( this, property, oldValue, newValue );
		for( PropertyChangeListener listener : listeners ) {
			listener.propertyChange( event );
		}
	}

}
