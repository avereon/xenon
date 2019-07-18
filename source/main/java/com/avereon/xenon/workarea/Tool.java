package com.avereon.xenon.workarea;

import com.avereon.settings.Settings;
import com.avereon.util.Configurable;
import com.avereon.util.LogUtil;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceEvent;
import com.avereon.xenon.resource.ResourceListener;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The Tool class is a control that "works on" a resource.
 */
public abstract class Tool extends Control implements Configurable {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final String ICON_PROPERTY = "icon";

	public static final String TITLE_PROPERTY = "title";

	public static final String DESCRIPTION_PROPERTY = "description";

	public static final Workpane.Placement DEFAULT_TOOL_PLACEMENT = Workpane.Placement.SMART;

	private static ToolInfo toolInfo = new ToolInfo();

	private ObjectProperty<Node> graphicProperty;

	private StringProperty titleProperty;

	private ObjectProperty<Node> closeGraphicProperty;

	private ObjectProperty<CloseOperation> closeOperation;

	private Resource resource;

	private WorkpaneView parent;

	private boolean allocated;

	private boolean displayed;

	private ResourceListener watcher;

	private Set<ToolListener> listeners;

	private Settings settings;

	public Tool( Resource resource ) {
		this( resource, null );
	}

	public Tool( Resource resource, String title ) {
		this.graphicProperty = new SimpleObjectProperty<>();
		this.titleProperty = new SimpleStringProperty();
		this.closeGraphicProperty = new SimpleObjectProperty<>(  );
		this.closeOperation = new SimpleObjectProperty<>();
		this.listeners = new CopyOnWriteArraySet<>();
		this.resource = resource;

		Rectangle clip = new Rectangle(  );
		clip.widthProperty().bind( widthProperty() );
		clip.heightProperty().bind( heightProperty() );
		setClip( clip );

		getStyleClass().add( "tool" );
		setTitle( title );
	}

	public Resource getResource() {
		return resource;
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
	 * <dd>Do nothing. This requires the program to call WorkPane.removeTool()
	 * usually as part of the <code>toolClosing</code> method of a registered
	 * <code>ToolListener</code> object.
	 * </dl>
	 * <p>
	 * Before performing the specified close operation, the tool fires a tool
	 * closing event.
	 *
	 * @param operation One of the following constants: <code>REMOVE</code> or <code>NOTHING</code>
	 * @see #addToolListener
	 * @see #getCloseOperation
	 */
	public void setCloseOperation( CloseOperation operation ) {
		closeOperation.set( operation );
	}

	public ObjectProperty<CloseOperation> closeOperation() {
		return closeOperation;
	}

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
	 * @return
	 */
	public boolean isActiveInToolView() {
		WorkpaneView view = getToolView();
		return view != null && view.getActiveTool() == this;
	}

	/**
	 * Set this tool as the active tool in the tool view.
	 */
	public void setActiveInToolView() {
		WorkpaneView view = getToolView();
		if( view != null ) view.setActiveTool( this );
	}

	public WorkpaneView getToolView() {
		return parent;
	}

	public void setToolView( WorkpaneView parent ) {
		this.parent = parent;
		if( settings != null ) settings.set( UiFactory.PARENT_WORKPANEVIEW_ID, parent == null ? null : parent.getViewId() );
	}

	public Workpane getWorkpane() {
		WorkpaneView view = getToolView();
		return view == null ? null : view.getWorkpane();
	}

	public boolean isAllocated() {
		return allocated;
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public boolean isActive() {
		return parent != null && parent.getActiveTool() == this;
	}

	public void close() {
		Platform.runLater( () -> getWorkpane().removeTool( this, true ) );
	}

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

	public void setSettings( Settings settings ) {
		if( this.settings != null ) return;

		this.settings = settings;
	}

	public Settings getSettings() {
		return settings;
	}

	public void addToolListener( ToolListener listener ) {
		listeners.add( listener );
	}

	public void removeToolListener( ToolListener listener ) {
		listeners.remove( listener );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( "title=\"" );
		builder.append( getTitle() );
		builder.append( "\"" );

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
	 * Called when the resource is ready to be used by the tool. This method is
	 * called each time the resource edited by this tool is opened. If it is
	 * opened another time it may have different parameters.
	 *
	 * @param parameters
	 */
	protected void resourceReady( ToolParameters parameters ) throws ToolException {}

	/**
	 * Called when the resource data is refreshed.
	 */
	protected void resourceRefreshed() throws ToolException {}

	/**
	 * Allocate the tool.
	 */
	public final void callAllocate() {
		Workpane pane = getWorkpane();
		try {
			getResource().addResourceListener( watcher = new ResourceWatcher() );
			allocate();
			allocated = true;
			pane.queueEvent( new WorkpaneToolEvent( pane, WorkpaneEvent.Type.TOOL_ADDED, pane, this ) );
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
			pane.queueEvent( new WorkpaneToolEvent( pane, WorkpaneEvent.Type.TOOL_DISPLAYED, pane, this ) );
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
			pane.queueEvent( new WorkpaneToolEvent( pane, WorkpaneEvent.Type.TOOL_ACTIVATED, pane, this ) );
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
			pane.queueEvent( new WorkpaneToolEvent( pane, WorkpaneEvent.Type.TOOL_DEACTIVATED, pane, this ) );
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
			pane.queueEvent( new WorkpaneToolEvent( pane, WorkpaneEvent.Type.TOOL_CONCEALED, pane, this ) );
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
			getResource().removeResourceListener( watcher );
			pane.queueEvent( new WorkpaneToolEvent( pane, WorkpaneEvent.Type.TOOL_REMOVED, pane, this ) );
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

	/**
	 * Called when the resource is ready to be used by the tool.
	 */
	public void callResourceReady( ToolParameters parameters ) {
		try {
			resourceReady( parameters );
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

	/**
	 * Called when the resource is ready to be used by the tool.
	 */
	public void callResourceRefreshed() {
		try {
			resourceRefreshed();
		} catch( ToolException exception ) {
			log.error( "Error deallocating tool", exception );
		}
	}

	public final void fireToolClosingEvent( ToolEvent event ) throws ToolVetoException {
		ToolVetoException exception = null;
		for( ToolListener listener : listeners ) {
			try {
				listener.toolClosing( event );
			} catch( ToolVetoException vetoException ) {
				if( exception == null ) exception = vetoException;
			}
		}
		if( exception != null ) throw exception;
	}

	public final void fireToolClosedEvent( ToolEvent event ) {
		for( ToolListener listener : listeners ) {
			listener.toolClosed( event );
		}
	}

	private class ResourceWatcher implements ResourceListener {

		@Override
		public void eventOccurred( ResourceEvent event ) {
			switch( event.getType() ) {
				case REFRESHED: {
					Tool.this.callResourceRefreshed();
					break;
				}
				case CLOSED: {
					Tool.this.close();
					break;
				}
			}
		}

	}

}
