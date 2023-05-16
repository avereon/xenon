package com.avereon.xenon.workspace;

import com.avereon.skill.WritableIdentity;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.*;
import com.avereon.zarra.event.FxEventWrapper;
import javafx.beans.property.*;
import javafx.geometry.Side;
import javafx.scene.input.TransferMode;
import lombok.CustomLog;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public class Workarea implements WritableIdentity {

	private final StringProperty name;

	private final BooleanProperty active;

	private final ObjectProperty<Workspace> workspace;

	private final Workpane workpane;

	// private MenuBar extraMenuBarItems

	// private ToolBar extraToolBarItems

	public Workarea() {
		name = new SimpleStringProperty( this, "name", "" );
		active = new SimpleBooleanProperty( this, "active", false );
		workspace = new SimpleObjectProperty<>( this, "workspace" );

		workpane = new Workpane();
		workpane.setEdgeSize( UiFactory.PAD );
		workpane.addEventHandler( ToolEvent.ACTIVATED, this::doSetCurrentAsset );
		workpane.addEventHandler( ToolEvent.CONCEALED, this::doClearCurrentAsset );
		workpane.addEventHandler( ToolEvent.ANY, this::doDispatchToolEventToWorkspace );

		// TODO Could be moved to UiFactory
		workpane.setOnToolDrop( new DropHandler( this ) );
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
	public String getUid() {
		return workpane.getUid();
	}

	@Override
	public void setUid( String id ) {
		workpane.setUid( id );
	}

	@Override
	public String toString() {
		return getName();
	}

	private void doSetCurrentAsset( ToolEvent e ) {
		ProgramTool tool = (ProgramTool)e.getTool();
		if( !tool.changeCurrentAsset() ) return;
		getWorkspace().getProgram().getAssetManager().setCurrentAsset( tool.getAsset() );
	}

	private void doClearCurrentAsset( ToolEvent e ) {
		getWorkspace().getProgram().getAssetManager().setCurrentAsset( null );
	}

	private void doDispatchToolEventToWorkspace( ToolEvent e ) {
		getWorkspace().getEventBus().dispatch( new FxEventWrapper( e ) );
	}

	private static class DropHandler implements DropListener {

		private final Workarea workarea;

		public DropHandler( Workarea workarea ) {
			this.workarea = workarea;
		}

		@Override
		public TransferMode[] getSupportedModes( Tool tool ) {
			ToolInstanceMode instanceMode = getProgram().getToolManager().getToolInstanceMode( ((ProgramTool)tool).getClass() );
			return instanceMode == ToolInstanceMode.SINGLETON ? MOVE_ONLY : TransferMode.ANY;
		}

		@Override
		public void handleDrop( DropEvent event ) throws Exception {
			TransferMode mode = event.getTransferMode();
			Tool sourceTool = event.getSource();
			WorkpaneView targetView = event.getTarget();
			int index = event.getIndex();
			Side side = event.getSide();
			List<URI> uris = event.getUris();
			boolean droppedOnArea = event.getArea() == DropEvent.Area.TOOL_AREA;

			if( sourceTool == null ) {
				// NOTE If the event source is null the drag came from outside the program
				uris.forEach( u -> getProgram().getAssetManager().openAsset( u, targetView, side ) );
			} else {
				if( mode == TransferMode.MOVE ) {
					// Check if being dropped on self
					if( droppedOnArea && side == null && sourceTool == targetView.getActiveTool() ) return;
					Workpane.moveTool( sourceTool, targetView, side, index );
				} else if( mode == TransferMode.COPY ) {
					getProgram().getAssetManager().openAsset( sourceTool.getAsset(), targetView, side );
				}
			}
		}

		private Xenon getProgram() {
			return workarea.getWorkspace().getProgram();
		}

	}

}
