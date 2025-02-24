package com.avereon.xenon.workspace;

import com.avereon.skill.WritableIdentity;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.ToolInstanceMode;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.workpane.*;
import com.avereon.zarra.event.FxEventWrapper;
import javafx.beans.property.*;
import javafx.geometry.Side;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.*;
import lombok.CustomLog;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public class Workarea extends Workpane implements WritableIdentity {

	private final StringProperty icon;

	private final StringProperty name;

	private final IntegerProperty order;

	private final ObjectProperty<Paint> paint;

	private final ObjectProperty<Color> color;

	private final BooleanProperty active;

	private final ObjectProperty<Workspace> workspace;

	public Workarea() {
		LinearGradient gradient = new LinearGradient( 0, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, new Stop( 0, Color.BLUEVIOLET ), new Stop( 1, Color.TRANSPARENT ) );

		icon = new SimpleStringProperty( this, "icon" );
		name = new SimpleStringProperty( this, "name" );
		order = new SimpleIntegerProperty( this, "order" );
		paint = new SimpleObjectProperty<>( this, "paint", gradient );
		color = new SimpleObjectProperty<>( this, "color", Color.valueOf( "#206080" ) );
		active = new SimpleBooleanProperty( this, "active" );
		workspace = new SimpleObjectProperty<>( this, "workspace" );

		setEdgeSize( UiFactory.PAD );

		visibleProperty().bind( activeProperty() );

		addEventHandler( ToolEvent.ACTIVATED, this::doSetCurrentAsset );
		addEventHandler( ToolEvent.CONCEALED, this::doClearCurrentAsset );
		addEventHandler( ToolEvent.ANY, this::doDispatchToolEventToWorkspace );

		// TODO Could be moved to UiFactory
		setOnToolDrop( new DropHandler( this ) );
	}

	public final StringProperty iconProperty() {
		return icon;
	}

	public final String getIcon() {
		return icon.get();
	}

	public final void setIcon( String icon ) {
		this.icon.set( icon );
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

	public final IntegerProperty orderProperty() {
		return order;
	}

	public final int getOrder() {
		return order.get();
	}

	public final void setOrder( int order ) {
		this.order.set( order );
	}

	public final ObjectProperty<Paint> paintProperty() {
		return paint;
	}

	public final Paint getPaint() {
		return paint.get();
	}

	public final void setPaint( Paint paint ) {
		this.paint.set( paint );
	}

	public final ObjectProperty<Color> colorProperty() {
		return color;
	}

	public final Color getColor() {
		return color.get();
	}

	public final void setColor( Color color ) {
		this.color.set( color );
	}

	public final BooleanProperty activeProperty() {
		return active;
	}

	public final boolean isActive() {
		return active.get();
	}

	public final void setActive( boolean active ) {
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

	public Xenon getProgram() {
		return getWorkspace().getProgram();
	}

	public Set<Asset> getAssets() {
		return getTools().stream().map( Tool::getAsset ).collect( Collectors.toSet() );
	}

	public Set<Asset> getModifiedAssets() {
		return getAssets().stream().filter( Asset::isNewOrModified ).collect( Collectors.toSet() );
	}

	//	@Override
	//	public String getUid() {
	//		return super.getUid();
	//	}
	//
	//	@Override
	//	public void setUid( String id ) {
	//		super.setUid( id );
	//	}

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
		public void handleDrop( DropEvent event ) {
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
			return workarea.getProgram();
		}

	}

}
