package com.parallelsymmetry.essence.work;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.configuration2.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// FIXME Observable was implemented in hopes the name would be updated in the workarea selector
public class Workarea implements Observable {

	private String id;

	private StringProperty name = new SimpleStringProperty();

	private boolean active;

	private Workspace workspace;

	private Set<PropertyChangeListener> propertyChangeListeners;

	private Set<InvalidationListener> invalidationListeners;

	private Configuration configuration;

	public Workarea() {
		propertyChangeListeners = new CopyOnWriteArraySet<>();
		invalidationListeners = new CopyOnWriteArraySet<>();
	}

	public String getId() {
		return id;
	}

	public StringProperty getNameValue() {
		return name;
	}

	public String getName() {
		return name.get();
	}

	public void setName( String newName ) {
		String oldName = name.get();
		name.set( newName );
		configuration.setProperty( "name", newName );
		firePropertyChange( "name", oldName, newName );
	}

	public boolean isActive() {
		return active;
	}

	public void setActive( boolean active ) {
		this.active = active;
		configuration.setProperty( "active", active );
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
		propertyChangeListeners.add( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener ) {
		propertyChangeListeners.remove( listener );
	}

	public void setConfiguration( Configuration configuration ) {
		if( this.configuration != null ) return;

		this.configuration = configuration;

		id = configuration.getString( "id" );
		setName( configuration.getString( "name" ) );
		setActive( configuration.getBoolean( "active", false ) );
	}

	@Override
	public String toString() {
		return getName();
	}

	private void firePropertyChange( String property, Object oldValue, Object newValue ) {
		PropertyChangeEvent event = new PropertyChangeEvent( this, property, oldValue, newValue );
		for( PropertyChangeListener listener : propertyChangeListeners ) {
			listener.propertyChange( event );
		}
		fireInvalidated();
	}

	private void fireInvalidated() {
		for( InvalidationListener listener : invalidationListeners ) {
			listener.invalidated( this );
		}
	}

	@Override
	public void addListener( InvalidationListener listener ) {
		invalidationListeners.add( listener );
	}

	@Override
	public void removeListener( InvalidationListener listener ) {
		invalidationListeners.remove( listener );
	}

}
