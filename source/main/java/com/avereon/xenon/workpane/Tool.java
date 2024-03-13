package com.avereon.xenon.workpane;

import com.avereon.event.EventHandler;
import com.avereon.log.LazyEval;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetEvent;
import com.avereon.zarra.javafx.Fx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import lombok.CustomLog;
import lombok.Getter;

/**
 * The Tool class is a pane that "works on" an asset.
 */
@CustomLog
public abstract class Tool extends StackPane {

	public static final String SETTINGS_TYPE_KEY = "type";

	public static final String ICON_PROPERTY = "icon";

	public static final String TITLE_PROPERTY = "title";

	public static final String DESCRIPTION_PROPERTY = "description";

	public static final Workpane.Placement DEFAULT_TOOL_PLACEMENT = Workpane.Placement.SMART;

	private static final ContextMenu EMPTY_CONTEXT_MENU = new ContextMenu();

	private static final ToolInfo toolInfo = new ToolInfo();

	private final ObjectProperty<Node> graphicProperty;

	private final StringProperty titleProperty;

	private final ObjectProperty<Node> contextGraphicProperty;

	private final ObjectProperty<Node> closeGraphicProperty;

	private final ObjectProperty<CloseOperation> closeOperation;

	private final Asset asset;

	private WorkpaneView parent;

	private boolean allocated;

	@Getter
	private boolean displayed;

	private EventHandler<AssetEvent> closer;

	public Tool( Asset asset ) {
		this.asset = asset;

		this.graphicProperty = new SimpleObjectProperty<>();
		this.titleProperty = new SimpleStringProperty();
		this.contextGraphicProperty = new SimpleObjectProperty<>();
		this.closeGraphicProperty = new SimpleObjectProperty<>();
		this.closeOperation = new SimpleObjectProperty<>( CloseOperation.REMOVE );
		getStyleClass().add( "tool" );

		// Tools are not allowed to paint outside their bounds
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind( widthProperty() );
		clip.heightProperty().bind( heightProperty() );
		setClip( clip );

		addEventFilter( MouseEvent.MOUSE_PRESSED, e -> getWorkpane().setActiveTool( this ) );
	}

	public final Asset getAsset() {
		return asset;
	}

	public final <T> T getAssetModel() {
		return asset.getModel();
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
	public Node getContextGraphic() {
		return contextGraphicProperty.getValue();
	}

	public void setContextGraphic( Node graphic ) {
		contextGraphicProperty.setValue( graphic );
	}

	public ObjectProperty<Node> contextGraphicProperty() {
		return contextGraphicProperty;
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

	/**
	 * Get the context menu actions as a list of Strings. A submenu can be defined
	 * by adding a List&lt;Object&gt; to the list.
	 *
	 * @return The context menu actions
	 */
	public ContextMenu getContextMenu() {
		return EMPTY_CONTEXT_MENU;
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

	public boolean isActive() {
		return parent != null && parent.getActiveTool() == this;
	}

	public void close() {
		doClose();
	}

	@SuppressWarnings( { "MethodDoesntCallSuperMethod" } )
	@Override
	public Tool clone() {
		Tool tool = null;

		try {
			tool = getClass().getConstructor().newInstance();
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error cloning tool: %s", LazyEval.of( () -> getClass().getName() ) );
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

	/**
	 * Allocate the tool. Called just after the tool is added to the tool view
	 * but before the {@link ToolEvent#ADDED} event is fired.
	 */
	protected void allocate() throws ToolException {}

	/**
	 * Display the tool. Called just after the tool is displayed but before the
	 * {@link ToolEvent#DISPLAYED} event is fired.
	 */
	protected void display() throws ToolException {}

	/**
	 * Activate the tool. Called just after the tool is activated but before the
	 * {@link ToolEvent#ACTIVATED} event is fired.
	 */
	protected void activate() throws ToolException {}

	/**
	 * Deactivate the tool. Called just after the tool is deactivated but before
	 * the {@link ToolEvent#DEACTIVATED} event is fired. The tool may be
	 * deactivated either by the tool parent being deactivated or by a different
	 * tool being activated.
	 */
	protected void deactivate() throws ToolException {}

	/**
	 * Conceal the tool. Called just after the tool is concealed but before the
	 * {@link ToolEvent#CONCEALED} event is fired. The tool may be concealed when
	 * the tool was visible and then is hidden by another tool or removed from the
	 * tool parent.
	 */
	protected void conceal() throws ToolException {}

	/**
	 * Deallocate the tool. Called just after the tool is removed from the tool
	 * view but before the {@link ToolEvent#REMOVED} event is fired.
	 */
	protected void deallocate() throws ToolException {}

	/**
	 * Determine if this tool is the last tool of its type for the tool asset.
	 *
	 * @return True if this is the last tool of its type, false otherwise.
	 */
	protected boolean isLastTool() {
		Asset asset = getAsset();
		return getWorkpane().getTools( getClass() ).stream().filter( t -> t.getAsset() == asset ).count() == 1;
	}

	/**
	 * Allocate the tool.
	 *
	 * @see #allocate
	 */
	final void callAllocate() {
		Workpane pane = getWorkpane();
		try {
			getAsset().register( AssetEvent.CLOSED, closer = ( e ) -> this.doClose() );
			allocate();
			allocated = true;
			triggerEvent( new ToolEvent( null, ToolEvent.ADDED, pane, this ) );
		} catch( ToolException exception ) {
			log.atError( exception ).log( "Error allocating tool" );
		}
	}

	/**
	 * Display the tool.
	 *
	 * @see #display
	 */
	final void callDisplay() {
		Workpane pane = getWorkpane();
		try {
			display();
			displayed = true;
			triggerEvent( new ToolEvent( null, ToolEvent.DISPLAYED, pane, this ) );
		} catch( ToolException exception ) {
			log.atError( exception ).log( "Error displaying tool" );
		}
	}

	/**
	 * Activate the tool.
	 *
	 * @see #activate
	 */
	final void callActivate() {
		Workpane pane = getWorkpane();
		try {
			activate();
			triggerEvent( new ToolEvent( null, ToolEvent.ACTIVATED, pane, this ) );
		} catch( ToolException exception ) {
			log.atError( exception ).log( "Error activating tool" );
		}
	}

	/**
	 * Deactivate the tool.
	 *
	 * @see #deactivate
	 */
	final void callDeactivate() {
		Workpane pane = getWorkpane();
		try {
			deactivate();
			triggerEvent( new ToolEvent( null, ToolEvent.DEACTIVATED, pane, this ) );
		} catch( ToolException exception ) {
			log.atError( exception ).log( "Error deactivating tool" );
		}
	}

	/**
	 * Conceal the tool.
	 *
	 * @see #conceal
	 */
	final void callConceal() {
		Workpane pane = getWorkpane();
		try {
			conceal();
			displayed = false;
			triggerEvent( new ToolEvent( null, ToolEvent.CONCEALED, pane, this ) );
		} catch( ToolException exception ) {
			log.atError( exception ).log( "Error concealing tool" );
		}
	}

	/**
	 * Deallocate the tool.
	 *
	 * @see #deallocate
	 */
	final void callDeallocate() {
		Workpane pane = getWorkpane();
		try {
			deallocate();
			allocated = false;
			triggerEvent( new ToolEvent( null, ToolEvent.REMOVED, pane, this ) );
			getAsset().getEventHub().unregister( AssetEvent.CLOSED, closer );
		} catch( ToolException exception ) {
			log.atError( exception ).log( "Error deallocating tool" );
		}
	}

	private void triggerEvent( ToolEvent event ) {
		fireEvent( getWorkpane().queueEvent( event ) );
	}

	private void doClose() {
		Workpane workpane = getWorkpane();
		if( workpane != null ) Fx.run( () -> workpane.closeTool( this, true ) );
	}

}
