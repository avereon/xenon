package com.parallelsymmetry.essence.workarea;

import com.parallelsymmetry.essence.settings.Settings;
import com.parallelsymmetry.essence.util.Configurable;
import com.parallelsymmetry.essence.workspace.ToolInstanceMode;
import com.parallelsymmetry.essence.worktool.Tool;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Workarea implements Configurable {

	private String id;

	private StringProperty name = new SimpleStringProperty();

	private BooleanProperty active = new SimpleBooleanProperty();

	private Workspace workspace;

	private Workpane workpane;

	private Set<PropertyChangeListener> propertyChangeListeners;

	private Settings settings;

	public Workarea() {
		workpane = new Workpane();
		propertyChangeListeners = new CopyOnWriteArraySet<>();
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

	public void addTool( Tool tool ) {
		Workpane workpane = getWorkpane();

		// TODO Lookup these two values from settings
		// Or have them passed in as values
		// Use the tool values as defaults
		Workpane.Placement placement = tool.getPlacement();
		ToolInstanceMode instanceMode = tool.getInstanceMode();

		Tool existingTool;
		if( instanceMode == ToolInstanceMode.SINGLETON && (existingTool = getExistingTool( tool )) != null ) {
			workpane.setActiveTool( existingTool );
		} else {
			// TODO Utilize the placement value to pick a place
			if( placement == Workpane.Placement.SMART) {
				workpane.getSmartView();
			}

			workpane.addTool( tool, true );
		}
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
	public void loadSettings( Settings settings ) {
		if( this.settings != null ) return;

		this.settings = settings;

		id = settings.getString( "id" );
		setName( settings.getString( "name" ) );
		setActive( settings.getBoolean( "active", false ) );
	}

	@Override
	public void saveSettings( Settings settings ) {}

	@Override
	public String toString() {
		return getName();
	}

	Workpane getWorkpane() {
		return workpane;
	}

	private Tool getExistingTool( Tool tool ) {
		for( Tool paneTool : workpane.getTools() ) {
			if( paneTool.getClass() == tool.getClass() ) return paneTool;
		}
		return null;
	}

	private void firePropertyChange( String property, Object oldValue, Object newValue ) {
		PropertyChangeEvent event = new PropertyChangeEvent( this, property, oldValue, newValue );
		for( PropertyChangeListener listener : propertyChangeListeners ) {
			listener.propertyChange( event );
		}
	}

}
