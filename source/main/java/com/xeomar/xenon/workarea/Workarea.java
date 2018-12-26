package com.xeomar.xenon.workarea;

import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;
import com.xeomar.xenon.UiFactory;
import com.xeomar.xenon.workspace.Workspace;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Workarea implements Configurable {

	private StringProperty name = new SimpleStringProperty( this, "name", "" );

	private BooleanProperty active = new SimpleBooleanProperty( this, "active", false );

	private Workspace workspace;

	private Workpane workpane;

	// private MenuBar extraMenuBarItems

	// private ToolBar extraToolBarItems

	private Settings settings;

	public Workarea() {
		workpane = new Workpane();
		workpane.setEdgeSize( UiFactory.PAD );
	}

	public final StringProperty nameProperty() {
		return name;
	}

	public final String getName() {
		return name.get();
	}

	public final void setName( String name ) {
		this.name.set( name );
		settings.set( "name", name );
	}

	public final BooleanProperty activeProperty() {
		return active;
	}

	public final boolean isActive() {
		return active.get();
	}

	public final void setActive( boolean active ) {
		this.active.set( active );
		settings.set( "active", active );
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
			settings.set( UiFactory.PARENT_WORKSPACE_ID, this.workspace.getSettings().getName() );
		} else {
			settings.set( UiFactory.PARENT_WORKSPACE_ID, null );
		}
	}

	@Override
	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;

		this.settings = settings;

		setName( settings.get( "name" ) );
		setActive( settings.get( "active", Boolean.class, false ) );
	}

	@Override
	public Settings getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		return getName();
	}

}
