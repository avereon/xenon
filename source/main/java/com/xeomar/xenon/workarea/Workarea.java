package com.xeomar.xenon.workarea;

import com.xeomar.xenon.IdGenerator;
import com.xeomar.xenon.ProgramSettings;
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

	public static final String PARENT_WORKSPACE_ID = "workspace-id";

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
			// FIXME I really don't like all of this in an event listener
			log.warn( "Workpane event: {}", event );
			switch( event.getType() ) {
				case EDGE_ADDED: {
					WorkpaneEdgeEvent edgeEvent = (WorkpaneEdgeEvent)event;
					String id = IdGenerator.getId();
					Settings settings = Workarea.this.settings.getNode( ProgramSettings.WORKPANEEDGE ).getNode( id );
					settings.set( "id", id );
					settings.set( "position", edgeEvent.getPosition() );
					edgeEvent.getEdge().setSettings( settings );
					break;
				}
				case EDGE_REMOVED: {
					((WorkpaneEdgeEvent)event).getEdge().getSettings().delete();
					break;
				}
				case VIEW_ADDED: {
					WorkpaneViewEvent viewEvent = (WorkpaneViewEvent)event;
					WorkpaneView view = viewEvent.getView();

					String id = IdGenerator.getId();
					Settings settings = Workarea.this.settings.getNode( ProgramSettings.WORKPANEVIEW ).getNode( id );
					settings.set( "id", id );
					settings.set( "t", getEdgeId( view.getEdge( Side.TOP ) ) );
					settings.set( "l", getEdgeId( view.getEdge( Side.LEFT ) ) );
					settings.set( "r", getEdgeId( view.getEdge( Side.RIGHT ) ) );
					settings.set( "b", getEdgeId( view.getEdge( Side.BOTTOM ) ) );

					view.setSettings( settings );
					break;
				}
				case VIEW_REMOVED: {
					break;
				}
			}
		}

		private String getEdgeId( WorkpaneEdge edge ) {
			String id = null;

			Settings settings = edge.getSettings();
			if( settings != null ) id = settings.get( "id" );

			// FIXME Do I want to do it this way...or just set ids on the edges
			if( edge.isWall() ) {
				Workpane pane = edge.getWorkpane();
				if( edge == pane.getWallEdge( Side.TOP ) ) id = "t";
				if( edge == pane.getWallEdge( Side.LEFT ) ) id = "l";
				if( edge == pane.getWallEdge( Side.RIGHT ) ) id = "r";
				if( edge == pane.getWallEdge( Side.BOTTOM ) ) id = "b";
			}

			return id;
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
			settings.set( PARENT_WORKSPACE_ID, this.workspace.getId() );
		} else {
			settings.set( PARENT_WORKSPACE_ID, null );
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
