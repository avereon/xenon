package com.xeomar.xenon.workarea;

import com.xeomar.xenon.IdGenerator;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.util.Configurable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workarea implements Configurable {

	private static final Logger log = LoggerFactory.getLogger( Workarea.class );

	private String id;

	private StringProperty name = new SimpleStringProperty();

	private BooleanProperty active = new SimpleBooleanProperty();

	private Workspace workspace;

	private Workpane workpane;

	// private MenuBar extraMenuBarItems

	// private ToolBar extraToolBarItems

	private Set<PropertyChangeListener> propertyChangeListeners;

	private Settings settings;

	public Workarea() {
		workpane = new Workpane();
		propertyChangeListeners = new CopyOnWriteArraySet<>();

		workpane.addWorkpaneListener( new WorkpaneWatcher() );
	}

	private class WorkpaneWatcher implements WorkpaneListener {

		@Override
		public void handle( WorkpaneEvent event ) throws WorkpaneVetoException {
			log.warn( "Workpane event: {}", event );
			switch( event.getType() ) {
				case EDGE_ADDED: {
					WorkpaneEdgeEvent edgeEvent = (WorkpaneEdgeEvent)event;
					String id = IdGenerator.getId();
					settings.getSettings( "workpane/edges/" + id );
					settings.set( "id", id );
					settings.set( "position", edgeEvent.getPosition() );
					edgeEvent.getEdge().setSettings( settings );
					break;
				}
				case EDGE_REMOVED: {
					((WorkpaneEdgeEvent)event).getEdge().getSettings();
					break;
				}
			}
		}

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name.get();
	}

	public void setName( String newName ) {
		String oldName = name.get();
		name.set( newName );
		settings.set( "name", newName );
		if( isActive() ) firePropertyChange( "name", oldName, newName );
	}

	public StringProperty getNameValue() {
		return name;
	}

	public boolean isActive() {
		return active.get();
	}

	public void setActive( boolean active ) {
		this.active.set( active );
		settings.set( "active", active );
	}

	public BooleanProperty getActiveValue() {
		return active;
	}

	public Workpane getWorkpane() {
		return workpane;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace( Workspace workspace ) {
		Workspace oldWorkspace = this.workspace;

		this.workspace = workspace;

		if( this.workspace != null ) {
			settings.set( "workspaceId", this.workspace.getId() );
		} else {
			settings.set( "workspaceId", null );
		}

		firePropertyChange( "workspace", oldWorkspace, this.workspace );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener ) {
		propertyChangeListeners.add( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener ) {
		propertyChangeListeners.remove( listener );
	}

	@Override
	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;

		this.settings = settings;

		id = settings.get( "id" );
		setName( settings.get( "name" ) );
		setActive( settings.getBoolean( "active", false ) );
	}

	@Override
	public Settings getSettings() {
		return settings;
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
	}

}
