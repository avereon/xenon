package com.avereon.xenon.workpane;

import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.util.LogUtil;
import com.avereon.xenon.OpenToolRequestParameters;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * The Tool class is a control that "works on" a asset.
 */
public abstract class Tool extends Control {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final String SETTINGS_TYPE_KEY = "type";

	public static final String ICON_PROPERTY = "icon";

	public static final String TITLE_PROPERTY = "title";

	public static final String DESCRIPTION_PROPERTY = "description";

	public static final Workpane.Placement DEFAULT_TOOL_PLACEMENT = Workpane.Placement.SMART;

	private static ToolInfo toolInfo = new ToolInfo();

	private ObjectProperty<Node> graphicProperty;

	private StringProperty titleProperty;

	private ObjectProperty<Node> closeGraphicProperty;

	private ObjectProperty<CloseOperation> closeOperation;

	private Asset asset;

	private WorkpaneView parent;

	private boolean allocated;

	private boolean displayed;

	private AssetWatcher watcher;

	public Tool( Asset asset ) {
		this( asset, null );
	}

	public Tool( Asset asset, String title ) {
		this.graphicProperty = new SimpleObjectProperty<>();
		this.titleProperty = new SimpleStringProperty();
		this.closeGraphicProperty = new SimpleObjectProperty<>();
		this.closeOperation = new SimpleObjectProperty<>( CloseOperation.REMOVE );
		getStyleClass().add( "tool" );

		this.asset = asset;
		setTitle( title );

		// Tools are not allowed to paint outside their bounds
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind( widthProperty() );
		clip.heightProperty().bind( heightProperty() );
		setClip( clip );
	}

	public Asset getAsset() {
		return asset;
	}

	public Workpane.Placement getPlacement() {
		return DEFAULT_TOOL_PLACEMENT;
	}

	/**
	 * Returns the operation that occurs when the user initiates a "close" on this
	 * tool.
	 *
	 * @return An integer indicating the tool close operation.
	 * @see #setCloseOperation
	 */
	public CloseOperation getCloseOperation() {
		return closeOperation.get();
	}

	/**
	 * Sets the operation that will happen by default when the user initiates a
	 * "close" on this tool. The possible choices are:
	 * <p>
	 * <dl>
	 * <dt><code>REMOVE</code>
	 * <dd>Automatically remove the tool from the workpane.
	 * <dt><code>NOTHING</code>
	 * <dd>Do nothing. This requires the program to call {@link Workpane#removeTool(Tool)}
	 * usually as part of an {@link EventHandler} that receives {@link ToolEvent#CLOSING} event.
	 * </dl>
	 * <p>
	 * Before performing the specified close operation, the tool fires a tool
	 * closing event.
	 *
	 * @param operation One of the following constants: <code>REMOVE</code> or <code>NOTHING</code>
	 * @see #getCloseOperation
	 */
	@SuppressWarnings( "unused" )
	public void setCloseOperation( CloseOperation operation ) {
		closeOperation.set( operation );
	}

	@SuppressWarnings( "unused" )
	public ObjectProperty<CloseOperation> closeOperation() {
		return closeOperation;
	}

	@SuppressWarnings( "unused" )
	public Node getGraphic() {
		return graphicProperty.getValue();
	}

	public void setGraphic( Node graphic ) {
		graphicProperty.setValue( graphic );
	}

	public ObjectProperty<Node> graphicProperty() {
		return graphicProperty;
	}

	public void setTitle( String title ) {
		titleProperty.setValue( title );
	}

	public String getTitle() {
		return titleProperty.getValue();
	}

	public StringProperty titleProperty() {
		return titleProperty;
	}

	@SuppressWarnings( "unused" )
	public Node getCloseGraphic() {
		return closeGraphicProperty.getValue();
	}

	public void setCloseGraphic( Node graphic ) {
		closeGraphicProperty.setValue( graphic );
	}

	public ObjectProperty<Node> closeGraphicProperty() {
		return closeGraphicProperty;
	}

	/**
	 * Check if this tool is the active tool in the tool view. If this tool has
	 * not been added to a tool view then this method returns false.
	 *
	 * @return If this tool is the active tool in the tool view
	 */
	@SuppressWarnings( "unused" )
	public boolean isActiveInToolView() {
		WorkpaneView view = getToolView();
		return view != null && view.getActiveTool() == this;
	}

	/**
	 * Set this tool as the active tool in the tool view.
	 */
	@SuppressWarnings( "unused" )
	public void setActiveInToolView() {
		WorkpaneView view = getToolView();
		if( view != null ) view.setActiveTool( this );
	}

	public WorkpaneView getToolView() {
		return parent;
	}

	void setToolView( WorkpaneView parent ) {
		this.parent = parent;
	}

	public Workpane getWorkpane() {
		WorkpaneView view = getToolView();
		return view == null ? null : view.getWorkpane();
	}

	@SuppressWarnings( "unused" )
	public boolean isAllocated() {
		return allocated;
	}

	public int getTabOrder() {
		return getToolView() == null ? -1 : getToolView().getTools().indexOf( this );
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public boolean isActive() {
		return parent != null && parent.getActiveTool() == this;
	}

	public void close() {
		Platform.runLater( () -> getWorkpane().closeTool( this, true ) );
	}

	@SuppressWarnings( { "MethodDoesntCallSuperMethod" } )
	@Override
	public Tool clone() {
		Tool tool = null;

		try {
			tool = getClass().getConstructor().newInstance();
		} catch( Exception exception ) {
			log.error( "Error cloning tool: " + getClass().getName(), exception );
		}

		return tool;
	}

	@SuppressWarnings( "StringBufferReplaceableByString" )
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( getClass().getSimpleName() );
		builder.append( "{" );
		builder.append( " id=\"" ).append( getId() ).append( "\"" );
		builder.append( " title=\"" ).append( getTitle() ).append( "\"" );
		builder.append( " }" );

		return builder.toString();
	}

	@Override
	protected Skin<Tool> createDefaultSkin() {
		return new ToolSkin( this );
	}

	/**
	 * Allocate the tool.
	 */
	protected void allocate() throws ToolException {}

	/**
	 * Display the tool.
	 */
	protected void display() throws ToolException {}

	/**
	 * Activate the tool.
	 */
	protected void activate() throws ToolException {}

	/**
	 * Deactivate the tool.
	 */
	protected void deactivate() throws ToolException {}

	/**
	 * Conceal the tool.
	 */
	protected void conceal() throws ToolException {}

	/**
	 * Deallocate the tool.
	 */
	protected void deallocate() throws ToolException {}

	/**
	 * Called when the asset is ready to be used by the tool. This method is
	 * called each time the asset edited by this tool is opened. If it is
	 * opened another time it may have different parameters.
	 *
	 * @param parameters The parameters used to open the tool
	 */
	protected void assetReady( OpenToolRequestParameters parameters ) throws ToolException {}

	/**
	 * Called when the asset data is refreshed.
	 */
	protected void assetRefreshed() throws ToolException {}

	/**
	 * Allocate the tool.
	 */
	public final void callAllocate() {
		Workpane pane = getWorkpane();
		try {
			getAsset().getEventHub().register( AssetEvent.ANY, watcher = new AssetWatcher() );
			allocate();
			allocated = true;
			fireEvent( pane.queueEvent( new ToolEvent( null, ToolEvent.ADDED, pane, this ) ) );
		} catch( ToolException exception ) {
			log.error( "Error allocating tool", exception );
		}
	}

	/**
	 * Display the tool.
	 */
	public final void callDisplay() {
		Workpane pane = getWorkpane();
		try {
			display();
			displayed = true;
			fireEvent( pane.queueEvent( new ToolEvent( null, ToolEvent.DISPLAYED, pane, this ) ) );
		} catch( ToolException exception ) {
			log.error( "Error displaying tool", exception );
		}
	}

	/**
	 * Activate the tool.
	 */
	public final void callActivate() {
		Workpane pane = getWorkpane();
		try {
			activate();
			fireEvent( pane.queueEvent( new ToolEvent( null, ToolEvent.ACTIVATED, pane, this ) ) );
		} catch( ToolException exception ) {
			log.error( "Error activating tool", exception );
		}
	}

	/**
	 * Deactivate the tool. Called when the tool is deactivated either by the tool
	 * parent being deactivated or by a different tool being activated.
	 */
	public final void callDeactivate() {
		Workpane pane = getWorkpane();
		try {
			deactivate();
			fireEvent( pane.queueEvent( new ToolEvent( null, ToolEvent.DEACTIVATED, pane, this ) ) );
		} catch( ToolException exception ) {
			log.error( "Error deactivating tool", exception );
		}
	}

	/**
	 * Conceal the tool. Called when the tool is visible and then is hidden by
	 * another tool or removed from the tool parent.
	 */
	public final void callConceal() {
		Workpane pane = getWorkpane();
		try {
			conceal();
			displayed = false;
			fireEvent( pane.queueEvent( new ToolEvent( null, ToolEvent.CONCEALED, pane, this ) ) );
		} catch( ToolException exception ) {
			log.error( "Error concealing tool", exception );
		}
	}

	/**
	 * Deallocate the tool. Called when the tool is removed from the tool parent.
	 */
	public final void callDeallocate() {
		Workpane pane = getWorkpane();
		try {
			deallocate();
			allocated = false;
			fireEvent( pane.queueEvent( new ToolEvent( null, ToolEvent.REMOVED, pane, this ) ) );
			getAsset().getEventHub().unregister( AssetEvent.ANY, watcher );
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

	/**
	 * Called when the asset is ready to be used by the tool.
	 */
	public void callAssetReady( OpenToolRequestParameters parameters ) {
		try {
			assetReady( parameters );
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

	/**
	 * Called when the asset is ready to be used by the tool.
	 */
	private void callAssetRefreshed() {
		try {
			assetRefreshed();
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

	private class AssetWatcher extends EventHub<AssetEvent> implements EventHandler<AssetEvent> {

		public AssetWatcher() {
			register( AssetEvent.REFRESHED, e -> Tool.this.callAssetRefreshed() );
			register( AssetEvent.CLOSED, e -> Tool.this.close() );
		}

	}

}
