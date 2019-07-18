package com.avereon.xenon.workarea;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class ToolTab extends Control {

	static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "selected" );

	private ToolPane pane;

	private Tool tool;

	private ReadOnlyBooleanWrapper selected;

	private ReadOnlyObjectWrapper<EventHandler<Event>> onCloseRequest;

	private ReadOnlyObjectWrapper<EventHandler<Event>> onSelectionChanged;

	public ToolTab( Tool tool ) {
		if( tool == null ) throw new NullPointerException( "Tool cannot be null" );
		this.tool = tool;
		getStyleClass().setAll( "tool-tab" );
	}

	public Tool getTool() {
		return tool;
	}

	public Node getContent() {
		return getTool();
	}

	public final ToolPane getToolPane() {
		return pane;
	}

	public final void setToolPane( ToolPane pane ) {
		this.pane = pane;
	}

	public Boolean isSelected() {
		return selected != null && selected.get();
	}

	public void setSelected( Boolean selected ) {
		selectedPropertyImpl().set( selected );
	}

	public ReadOnlyBooleanProperty selectedProperty() {
		return selectedPropertyImpl();
	}

	private ReadOnlyBooleanWrapper selectedPropertyImpl() {
		if( selected == null ) {
			selected = new ReadOnlyBooleanWrapper() {

				@Override
				protected void invalidated() {
					pseudoClassStateChanged( SELECTED_PSEUDOCLASS_STATE, isSelected() );
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

	private static final EventType<Event> SELECTION_CHANGED_EVENT = new EventType<>( Event.ANY, "SELECTION_CHANGED_EVENT" );

	public final EventHandler<Event> getOnSelectionChanged() {
		return onSelectionChanged == null ? null : onSelectionChanged.get();
	}

	public final void setOnSelectionChanged( EventHandler<Event> value ) {
		onSelectionChangedPropertyImpl().set( value );
	}

	public final ReadOnlyObjectProperty<EventHandler<Event>> onSelectionChangedProperty() {
		return onSelectionChangedPropertyImpl();
	}

	private ReadOnlyObjectWrapper<EventHandler<Event>> onSelectionChangedPropertyImpl() {
		if( onSelectionChanged == null ) {
			onSelectionChanged = new ReadOnlyObjectWrapper<>() {

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

	public static final EventType<Event> TOOL_TAB_CLOSE_REQUEST_EVENT = new EventType<>( Event.ANY, "TOOL_TAB_CLOSE_REQUEST_EVENT" );

	public EventHandler<Event> getOnCloseRequest() {
		if( onCloseRequest == null ) return null;
		return onCloseRequest.get();
	}

	public void setOnCloseRequest( EventHandler<Event> value ) {
		onCloseRequestPropertyImpl().set( value );
	}

	public final ReadOnlyObjectProperty<EventHandler<Event>> onCloseRequestProperty() {
		return onCloseRequestPropertyImpl();
	}

	private ReadOnlyObjectWrapper<EventHandler<Event>> onCloseRequestPropertyImpl() {

		if( onCloseRequest == null ) {
			onCloseRequest = new ReadOnlyObjectWrapper<>() {

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

	private void updateDisabled() {
		ToolPane pane = getToolPane();
		boolean disabled = isDisable() || (pane != null && pane.isDisabled());
		setDisabled( disabled );

		Node content = getContent();
		if( content != null ) content.setDisable( disabled );
	}

	private final InvalidationListener parentDisabledChangedListener = valueModel -> {
		updateDisabled();
	};

	@Override
	protected Skin<ToolTab> createDefaultSkin() {
		return new ToolTabSkin( this );
	}

}
