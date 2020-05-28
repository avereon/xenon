package com.avereon.xenon.workspace;

import com.avereon.settings.Settings;
import com.avereon.skill.Identified;
import com.avereon.venza.event.FxEventWrapper;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Set;
import java.util.stream.Collectors;

public class Workarea implements Identified {

	private StringProperty name = new SimpleStringProperty( this, "name", "" );

	private BooleanProperty active = new SimpleBooleanProperty( this, "active", false );

	private Workspace workspace;

	private Workpane workpane;

	// private MenuBar extraMenuBarItems

	// private ToolBar extraToolBarItems

	private Settings settings;

	private String id;

	public Workarea() {
		workpane = new Workpane();
		workpane.setEdgeSize( UiFactory.PAD );
		workpane.addEventHandler( ToolEvent.ACTIVATED, e -> workspace.getProgram().getAssetManager().setCurrentAsset( e.getTool().getAsset() ) );
		workpane.addEventHandler( ToolEvent.CONCEALED, e -> workspace.getProgram().getAssetManager().setCurrentAsset( null ) );
		workpane.addEventHandler( ToolEvent.ANY, e -> workspace.getEventBus().dispatch( new FxEventWrapper( e ) ) );
	}

	public final StringProperty nameProperty() {
		return name;
	}

	public final String getName() {
		return name.get();
	}

	public final void setName( String name ) {
		this.name.set( name );
		getSettings().set( "name", name );
	}

	public final BooleanProperty activeProperty() {
		return active;
	}

	public final boolean isActive() {
		return active.get();
	}

	public final void setActive( boolean active ) {
		workpane.setVisible( active );
		this.active.set( active );
		getSettings().set( "active", active );
	}

//	public Program getProgram() {
//		return getWorkspace().getProgram();
//	}

	public Workpane getWorkpane() {
		return workpane;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace( Workspace workspace ) {
		if( this.workspace != null ) {
			// Unhook the old workspace
		}

		this.workspace = workspace;

		if( this.workspace != null ) {
			getSettings().set( UiFactory.PARENT_WORKSPACE_ID, this.workspace.getSettings().getName() );
		} else {
			getSettings().set( UiFactory.PARENT_WORKSPACE_ID, null );
		}
	}

	public Set<Asset> getAssets() {
		return getWorkpane().getTools().stream().map( Tool::getAsset ).collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets() {
		return getAssets().stream().filter( Asset::isNewOrModified ).collect( Collectors.toSet() );
	}

	@Override
	public String getProductId() {
		return id;
	}

	@Override
	public void setProductId( String id ) {
		this.id = id;
	}

	public void updateFromSettings( Settings settings ) {
		// FIXME It would be nice if we did not need to keep the settings
		this.settings = settings;
		setName( settings.get( "name" ) );
		setActive( settings.get( "active", Boolean.class, false ) );
	}

	public Settings getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		return getName();
	}

}
