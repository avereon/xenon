package com.xeomar.xenon.workarea;

import com.xeomar.xenon.IdGenerator;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.UiManager;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.util.Configurable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workarea implements Configurable {

	private static final Logger log = LoggerFactory.getLogger( Workarea.class );

	private StringProperty name = new SimpleStringProperty();

	private BooleanProperty active = new SimpleBooleanProperty();

	private Workspace workspace;

	private Workpane workpane;

	// private MenuBar extraMenuBarItems

	// private ToolBar extraToolBarItems

	private Set<PropertyChangeListener> propertyChangeListeners;

	private Settings settings;

	public Workarea() {
		propertyChangeListeners = new CopyOnWriteArraySet<>();

		workpane = new Workpane();

		workpane.addWorkpaneListener( new WorkpaneWatcher() );
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
			settings.set( UiManager.PARENT_WORKSPACE_ID, this.workspace.getSettings().getName() );
		} else {
			settings.set( UiManager.PARENT_WORKSPACE_ID, null );
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

		setName( settings.get( "name" ) );
		setActive( settings.getBoolean( "active", false ) );

		createViewSettings( workpane.getDefaultView() );
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

	private void createEdgeSettings( WorkpaneEdge edge ) {
		Settings settings = Workarea.this.settings.getNode( ProgramSettings.EDGE, IdGenerator.getId() );
		settings.set( UiManager.PARENT_WORKPANE_ID, getSettings().getName() );
		settings.set( "position", edge.getPosition() );
		switch( edge.getOrientation() ) {
			case VERTICAL: {
				settings.set( "t", edge.getEdge( Side.TOP ).getEdgeId() );
				settings.set( "b", edge.getEdge( Side.BOTTOM ).getEdgeId() );
				break;
			}
			case HORIZONTAL: {
				settings.set( "l", edge.getEdge( Side.LEFT ).getEdgeId() );
				settings.set( "r", edge.getEdge( Side.RIGHT ).getEdgeId() );
				break;
			}
		}
		edge.setSettings( settings );
	}

	private void createViewSettings( WorkpaneView view ) {
		Settings settings = Workarea.this.settings.getNode( ProgramSettings.VIEW, IdGenerator.getId() );
		settings.set( UiManager.PARENT_WORKPANE_ID, getSettings().getName() );
		settings.set( "t", view.getEdge( Side.TOP ).getEdgeId() );
		settings.set( "l", view.getEdge( Side.LEFT ).getEdgeId() );
		settings.set( "r", view.getEdge( Side.RIGHT ).getEdgeId() );
		settings.set( "b", view.getEdge( Side.BOTTOM ).getEdgeId() );
		view.setSettings( settings );
	}

	private class WorkpaneWatcher implements WorkpaneListener {

		@Override
		public void handle( WorkpaneEvent event ) throws WorkpaneVetoException {
			switch( event.getType() ) {
				case EDGE_ADDED: {
					WorkpaneEdgeEvent edgeEvent = (WorkpaneEdgeEvent)event;
					WorkpaneEdge edge = edgeEvent.getEdge();
					if( edge.getSettings() != null ) return;
					createEdgeSettings( edge );
					break;
				}
				case EDGE_REMOVED: {
					((WorkpaneEdgeEvent)event).getEdge().getSettings().delete();
					break;
				}
				case VIEW_ADDED: {
					WorkpaneViewEvent viewEvent = (WorkpaneViewEvent)event;
					WorkpaneView view = viewEvent.getView();
					if( view.getSettings() != null ) return;
					createViewSettings( view );
					break;
				}
				case VIEW_REMOVED: {
					break;
				}
			}
		}

	}

}
