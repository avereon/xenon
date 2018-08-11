package com.xeomar.xenon.workarea;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.lang.ref.WeakReference;

public class ToolTab extends BorderPane {

	private Label label;

	private Button closeButton;

	private ObjectProperty<Tool> tool;

	public ToolTab( Tool tool ) {
		label = new Label();
		closeButton = new Button( "X" );

		setCenter( label );
		setRight( closeButton );

		setTool( tool );
	}

	private ReadOnlyBooleanWrapper selected;

	final void setSelected( boolean value ) {
		selectedPropertyImpl().set( value );
	}

	/**
	 * <p>Represents whether this tab is the currently selected tab,
	 * To change the selected Tab use {@code tabPane.getSelectionModel().select()}
	 * </p>
	 *
	 * @return true if selected
	 */
	public final boolean isSelected() {
		return selected != null && selected.get();
	}

	/**
	 * The currently selected tab.
	 *
	 * @return the selected tab
	 */
	public final ReadOnlyBooleanProperty selectedProperty() {
		return selectedPropertyImpl().getReadOnlyProperty();
	}

	private ReadOnlyBooleanWrapper selectedPropertyImpl() {
		if( selected == null ) {
			selected = new ReadOnlyBooleanWrapper() {

				@Override
				protected void invalidated() {
					if( getOnSelectionChanged() != null ) Event.fireEvent( ToolTab.this, new Event( SELECTION_CHANGED_EVENT ) );
				}

				@Override
				public Object getBean() {
					return ToolTab.this;
				}

				@Override
				public String getName() {
					return "selected";
				}
			};
		}
		return selected;
	}

	/**
	 * <p>The tool to show within the main ToolPane area.</p>
	 *
	 * @param tool the tool
	 */
	public final void setTool( Tool tool ) {
		toolProperty().set( tool );
		label.graphicProperty().bind( tool.graphicProperty() );
		label.textProperty().bind( tool.titleProperty() );
	}

	/**
	 * <p>The tool associated with the tab.</p>
	 *
	 * @return The tool associated with the tab.
	 */
	public final Tool getTool() {
		return tool == null ? null : tool.get();
	}

	/**
	 * <p>The tool associated with the tab.</p>
	 *
	 * @return the tool property
	 */
	public final ObjectProperty<Tool> toolProperty() {
		if( tool == null ) tool = new SimpleObjectProperty<>( this, "tool" );
		return tool;
	}

	/**
	 * <p>Called when the tab becomes selected or unselected.</p>
	 */
	public static final EventType<Event> SELECTION_CHANGED_EVENT = new EventType<>( Event.ANY, "SELECTION_CHANGED_EVENT" );

	private ObjectProperty<EventHandler<Event>> onSelectionChanged;

	/**
	 * Defines a function to be called when a selection changed has occurred on the tab.
	 * @param value the on selection changed event handler
	 */
	public final void setOnSelectionChanged(EventHandler<Event> value) {
		onSelectionChangedProperty().set(value);
	}

	/**
	 * The event handler that is associated with a selection on the tab.
	 *
	 * @return The event handler that is associated with a tab selection.
	 */
	public final EventHandler<Event> getOnSelectionChanged() {
		return onSelectionChanged == null ? null : onSelectionChanged.get();
	}

	/**
	 * The event handler that is associated with a selection on the tab.
	 * @return the on selection changed event handler property
	 */
	public final ObjectProperty<EventHandler<Event>> onSelectionChangedProperty() {
		if (onSelectionChanged == null) {
			onSelectionChanged = new ObjectPropertyBase<>() {

				@Override
				protected void invalidated() {
					setEventHandler( SELECTION_CHANGED_EVENT, get() );
				}

				@Override
				public Object getBean() {
					return ToolTab.this;
				}

				@Override
				public String getName() {
					return "onSelectionChanged";
				}
			};
		}
		return onSelectionChanged;
	}

	/**
	 * <p>Called when a user closes this tool tab. This is useful for freeing up memory.</p>
	 */
	public static final EventType<Event> CLOSED_EVENT = new EventType<>( Event.ANY, "TOOL_TAB_CLOSED" );

	private ObjectProperty<EventHandler<Event>> onClosed;

	private ReadOnlyObjectWrapper<ToolPane> toolPane;

	final void setToolPane( ToolPane value ) {
		tabPanePropertyImpl().set( value );
	}

	/**
	 * <p>A reference to the TabPane that contains this tab instance.</p>
	 *
	 * @return the TabPane
	 */
	public final ToolPane getToolPane() {
		return toolPane == null ? null : toolPane.get();
	}

	/**
	 * The TabPane that contains this tab.
	 *
	 * @return the TabPane property
	 */
	public final ReadOnlyObjectProperty<ToolPane> toolPaneProperty() {
		return tabPanePropertyImpl().getReadOnlyProperty();
	}

	private ReadOnlyObjectWrapper<ToolPane> tabPanePropertyImpl() {
		if( toolPane == null ) {
			toolPane = new ReadOnlyObjectWrapper<>( this, "tabPane" ) {

				private WeakReference<ToolPane> oldParent;

				@Override
				protected void invalidated() {
					if( oldParent != null && oldParent.get() != null ) {
						oldParent.get().disabledProperty().removeListener( parentDisabledChangedListener );
					}
					updateDisabled();
					ToolPane newParent = get();
					if( newParent != null ) {
						newParent.disabledProperty().addListener( parentDisabledChangedListener );
					}
					oldParent = new WeakReference<>( newParent );
					super.invalidated();
				}
			};
		}
		return toolPane;
	}

	private final InvalidationListener parentDisabledChangedListener = listener -> updateDisabled();

	private void updateDisabled() {
		boolean disabled = isDisable() || (getToolPane() != null && getToolPane().isDisabled());
		setDisabled( disabled );

		Tool tool = getTool();
		if( tool != null ) tool.setDisable( disabled );
	}

	/**
	 * Defines a function to be called when the tab is closed.
	 *
	 * @param value the on closed event handler
	 */
	public final void setOnClosed( EventHandler<Event> value ) {
		onClosedProperty().set( value );
	}

	/**
	 * The event handler that is associated with the tab when the tab is closed.
	 *
	 * @return The event handler that is associated with the tab when the tab is closed.
	 */
	public final EventHandler<Event> getOnClosed() {
		return onClosed == null ? null : onClosed.get();
	}

	/**
	 * The event handler that is associated with the tab when the tab is closed.
	 *
	 * @return the on closed event handler property
	 */
	public final ObjectProperty<EventHandler<Event>> onClosedProperty() {
		if( onClosed == null ) {
			onClosed = new ObjectPropertyBase<>() {

				@Override
				protected void invalidated() {
					setEventHandler( CLOSED_EVENT, get() );
				}

				@Override
				public Object getBean() {
					return ToolTab.this;
				}

				@Override
				public String getName() {
					return "onClosed";
				}

			};
		}

		return onClosed;
	}

	/**
	 * Called when there is an external request to close this {@code Tab}.
	 * The installed event handler can prevent tab closing by consuming the
	 * received event.
	 *
	 * @since JavaFX 8.0
	 */
	public static final EventType<Event> TAB_CLOSE_REQUEST_EVENT = new EventType<>( Event.ANY, "TOOL_TAB_CLOSE_REQUEST_EVENT" );

	/**
	 * Called when there is an external request to close this {@code Tab}.
	 * The installed event handler can prevent tab closing by consuming the
	 * received event.
	 *
	 * @since JavaFX 8.0
	 */
	private ObjectProperty<EventHandler<Event>> onCloseRequest;

	public final ObjectProperty<EventHandler<Event>> onCloseRequestProperty() {
		if( onCloseRequest == null ) {
			onCloseRequest = new ObjectPropertyBase<>() {

				@Override
				protected void invalidated() {
					setEventHandler( TAB_CLOSE_REQUEST_EVENT, get() );
				}

				@Override
				public Object getBean() {
					return ToolTab.this;
				}

				@Override
				public String getName() {
					return "onCloseRequest";
				}

			};
		}

		return onCloseRequest;
	}

	public EventHandler<Event> getOnCloseRequest() {
		return onCloseRequest == null ? null : onCloseRequest.get();
	}

	public void setOnCloseRequest( EventHandler<Event> value ) {
		onCloseRequestProperty().set( value );
	}

}
