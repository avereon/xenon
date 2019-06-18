package com.xeomar.xenon.workarea;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Labeled;

import java.lang.ref.WeakReference;

public class ToolTab extends Labeled {

	private BooleanProperty selected;

	private ReadOnlyObjectWrapper<ToolPane> pane;

	private SimpleObjectProperty<Tool> tool;

	public ToolTab( Tool tool ) {
		this.selected = new SimpleBooleanProperty( this, "selected" );
		this.tool = new SimpleObjectProperty<>( this, "tool", tool );

		graphicProperty().bind( tool.graphicProperty() );
		textProperty().bind( tool.titleProperty() );

		setOnCloseRequest( event -> {
			event.consume();
			tool.close();
		} );
	}

	public final ToolPane getToolPane() {
		return pane == null ? null : pane.get();
	}

	public final void setToolPane( ToolPane pane ) {
		toolPanePropertyImpl().set( pane );
	}

	public final ReadOnlyObjectProperty<ToolPane> tabPaneProperty() {
		return toolPanePropertyImpl().getReadOnlyProperty();
	}

	public ObservableBooleanValue selectedProperty() {
		return selected;
	}

	public Boolean isSelected() {
		return selected.get();
	}

	public void setSelected( Boolean selected ) {
		this.selected.set( selected );
	}

	public Node getContent() {
		return getTool();
	}

	public Tool getTool() {
		return tool.get();
	}

	public static final EventType<Event> TOOL_TAB_CLOSE_REQUEST_EVENT = new EventType<>( Event.ANY, "TOOL_TAB_CLOSE_REQUEST_EVENT" );

	/**
	 * Called when there is a request to close this {@code ToolTab}. The installed
	 * event handler can prevent tab closing by consuming the received event.
	 */
	private ObjectProperty<EventHandler<Event>> onCloseRequest;

	public final ObjectProperty<EventHandler<Event>> onCloseRequestProperty() {
		if( onCloseRequest == null ) {
			onCloseRequest = new ObjectPropertyBase<>() {

				@Override
				protected void invalidated() {
					setEventHandler( TOOL_TAB_CLOSE_REQUEST_EVENT, get() );
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
		if( onCloseRequest == null ) return null;
		return onCloseRequest.get();
	}

	public void setOnCloseRequest( EventHandler<Event> value ) {
		onCloseRequestProperty().set( value );
	}

	private void updateDisabled() {
		ToolPane pane = getToolPane();
		boolean disabled = isDisable() || (pane != null && pane.isDisabled());
		setDisabled( disabled );

		Node content = getContent();
		if( content != null ) content.setDisable( disabled );
	}

	private ReadOnlyObjectWrapper<ToolPane> toolPanePropertyImpl() {
		if( pane == null ) {
			pane = new ReadOnlyObjectWrapper<>( this, "pane" ) {

				private WeakReference<ToolPane> oldPane;

				@Override
				protected void invalidated() {
					if( oldPane != null && oldPane.get() != null ) oldPane.get().disabledProperty().removeListener( parentDisabledChangedListener );

					updateDisabled();
					ToolPane newPane = get();
					if( newPane != null ) newPane.disabledProperty().addListener( parentDisabledChangedListener );

					oldPane = new WeakReference<>( newPane );
					super.invalidated();
				}

			};
		}

		return pane;
	}

	private final InvalidationListener parentDisabledChangedListener = valueModel -> {
		updateDisabled();
	};

}
