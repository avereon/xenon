package com.avereon.xenon.workspace;

import com.avereon.settings.Settings;
import com.avereon.skill.WritableIdentity;
import com.avereon.venza.event.FxEventWrapper;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import javafx.beans.property.*;

import java.util.Set;
import java.util.stream.Collectors;

public class Workarea implements WritableIdentity {

	private final StringProperty name;

	private final BooleanProperty active;

	private final ObjectProperty<Workspace> workspace;

	private final Workpane workpane;

	// private MenuBar extraMenuBarItems

	// private ToolBar extraToolBarItems

	private String id;

	public Workarea() {
		name = new SimpleStringProperty( this, "name", "" );
		active = new SimpleBooleanProperty( this, "active", false );
		workspace = new SimpleObjectProperty<>( this, "workspace" );

		workpane = new Workpane();
		workpane.setEdgeSize( UiFactory.PAD );
		workpane.addEventHandler( ToolEvent.ACTIVATED, e -> getWorkspace().getProgram().getAssetManager().setCurrentAsset( e.getTool().getAsset() ) );
		workpane.addEventHandler( ToolEvent.CONCEALED, e -> getWorkspace().getProgram().getAssetManager().setCurrentAsset( null ) );
		workpane.addEventHandler( ToolEvent.ANY, e -> getWorkspace().getEventBus().dispatch( new FxEventWrapper( e ) ) );
	}

	public final StringProperty nameProperty() {
		return name;
	}

	public final String getName() {
		return name.get();
	}

	public final void setName( String name ) {
		this.name.set( name );
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
	}

	public final ObjectProperty<Workspace> workspaceProperty() {
		return workspace;
	}

	public Workspace getWorkspace() {
		return workspace.get();
	}

	public void setWorkspace( Workspace workspace ) {
		if( getWorkspace() != null ) {
			// Disconnect the old workspace
		}

		this.workspace.set( workspace );

		if( getWorkspace() != null ) {
			// Connect the new workspace
		}
	}

	public Workpane getWorkpane() {
		return workpane;
	}

	public Set<Asset> getAssets() {
		return getWorkpane().getTools().stream().map( Tool::getAsset ).collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets() {
		return getAssets().stream().filter( Asset::isNewOrModified ).collect( Collectors.toSet() );
	}

	@Override
	public String getProductId() {
		return workpane.getProductId();
	}

	@Override
	public void setProductId( String id ) {
		workpane.setProductId( id );
	}

	// TODO Could this be moved to UiFactory?
	public void updateFromSettings( Settings settings ) {
		setName( settings.get( "name" ) );
		setActive( settings.get( "active", Boolean.class, false ) );

		nameProperty().addListener( ( v, o, n ) -> settings.set( "name", n ) );
		activeProperty().addListener( ( v, o, n ) -> settings.set( "active", n ) );
		workspaceProperty().addListener( (v,o,n) -> settings.set( UiFactory.PARENT_WORKSPACE_ID, n == null ? null : n.getProductId() ) );
	}

	@Override
	public String toString() {
		return getName();
	}

}
