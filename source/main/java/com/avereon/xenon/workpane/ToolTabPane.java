package com.avereon.xenon.workpane;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Side;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.input.DragEvent;
import lombok.CustomLog;
import lombok.Getter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The ToolPane class provides a custom tab pane component for the program
 * allowing for extended and custom capabilities.
 */
@CustomLog
public class ToolTabPane extends Control {

	static final PseudoClass ACTIVE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "active" );

	@Getter
	private final ObservableList<ToolTab> tabs;

	private final ObjectProperty<SingleSelectionModel<ToolTab>> selectionModelProperty;

	private ReadOnlyBooleanWrapper active;

	public ToolTabPane() {
		getStyleClass().addAll( "tool-pane" );
		selectionModelProperty = new SimpleObjectProperty<>( this, "selectionModel" );
		selectionModelProperty.set( new ToolPaneSelectionModel( this ) );
		tabs = FXCollections.observableArrayList();
	}

	public final SingleSelectionModel<ToolTab> getSelectionModel() {
		return selectionModelProperty.get();
	}

	public boolean isActive() {
		return active != null && active.get();
	}

	public void setActive( boolean active ) {
		activeProperty().set( active );
	}

	public ReadOnlyBooleanWrapper activeProperty() {
		if( active == null ) {
			active = new ReadOnlyBooleanWrapper() {

				@Override
				protected void invalidated() {
					pseudoClassStateChanged( ACTIVE_PSEUDOCLASS_STATE, isActive() );
					//if( getOnActivatedChanged() != null ) Event.fireEvent( ToolTab.this, new Event( ACTIVATED_CHANGED_EVENT ) );
				}

				@Override
				public Object getBean() {
					return ToolTabPane.this;
				}

				@Override
				public String getName() {
					return "active";
				}
			};
		}
		return active;
	}

	@Override
	protected Skin<ToolTabPane> createDefaultSkin() {
		return new ToolTabPaneSkin( this );
	}

	protected Tool getTool() {
		return getSelectionModel().getSelectedItem().getTool();
	}

	protected WorkpaneView getWorkpaneView() {
		return (WorkpaneView)getParent();
	}

	protected Workpane getWorkpane() {
		return getWorkpaneView().getWorkpane();
	}

	/**
	 * The three workpane components (tab, header, toolarea) funnel drop events to
	 * this method.
	 *
	 * @param event The drag event for the drop
	 * @param area The workpane {@link DropEvent.Area} dropped on
	 * @param index The tab index
	 * @param side Which side of the tool area
	 */
	void handleDrop( DragEvent event, DropEvent.Area area, int index, Side side ) {
		try {
			Object gestureSource = event.getGestureSource();
			Tool sourceTool = (gestureSource instanceof ToolTab) ? ((ToolTab)gestureSource).getTool() : null;

			List<URI> uris = new ArrayList<>();
			if( event.getDragboard().getUrl() != null ) uris.add( new URI( event.getDragboard().getUrl() ) );
			event.getDragboard().getFiles().forEach( f -> uris.add( f.toURI() ) );

			getWorkpane().handleDrop( new DropEvent( this,
				DropEvent.DROP,
				getWorkpane(),
				event.getTransferMode(),
				sourceTool,
				getWorkpaneView(),
				area,
				index,
				side,
				uris
			) );
		} catch( Exception exception ) {
			log.atError( exception).log( "Error handling tool drop" );
		} finally {
			event.setDropCompleted( true );
			event.consume();
		}
	}

	private static class ToolPaneSelectionModel extends SingleSelectionModel<ToolTab> {

		private final ToolTabPane pane;

		ToolPaneSelectionModel( ToolTabPane pane ) {
			if( pane == null ) throw new NullPointerException( "ToolPane can not be null" );

			this.pane = pane;

			ListChangeListener<ToolTab> listener = ( change ) -> {
				while( change.next() ) {
					for( ToolTab tab : change.getRemoved() ) {
						if( tab != null && !pane.getTabs().contains( tab ) && tab.isSelected() ) {
							tab.setSelected( false );
							int fromIndex = change.getFrom();
							ToolTab next = findNearestAvailableTab( fromIndex );
							if( next != null ) select( next );
						}
					}

					if( (change.wasAdded() || change.wasRemoved()) && this.getSelectedIndex() != pane.getTabs().indexOf( this.getSelectedItem() ) ) {
						this.clearAndSelect( pane.getTabs().indexOf( this.getSelectedItem() ) );
					}
				}

				if( this.getSelectedIndex() == -1 && this.getSelectedItem() == null && !pane.getTabs().isEmpty() ) {
					ToolTab next = this.findNearestAvailableTab( 0 );
					if( next != null ) select( next );
				} else if( pane.getTabs().isEmpty() ) {
					this.clearSelection();
				}
			};

			if( pane.getTabs() != null ) pane.getTabs().addListener( listener );
		}

		@Override
		protected ToolTab getModelItem( int index ) {
			ObservableList<ToolTab> list = pane.getTabs();
			if( list == null ) return null;
			return index >= 0 && index < list.size() ? list.get( index ) : null;
		}

		@Override
		protected int getItemCount() {
			return pane.getTabs().size();
		}

		@Override
		public void select( int index ) {
			ToolTab tab = getModelItem( index );
			if( index < 0 || (getItemCount() > 0 && index >= getItemCount()) || (index == getSelectedIndex() && tab != null && tab.isSelected()) ) {
				return;
			}

			// Unselect the old tab
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) {
				pane.getTabs().get( getSelectedIndex() ).setSelected( false );
			}

			setSelectedIndex( index );
			if( tab != null ) setSelectedItem( tab );

			// Select the new tab
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) {
				pane.getTabs().get( getSelectedIndex() ).setSelected( true );
			}

			pane.notifyAccessibleAttributeChanged( AccessibleAttribute.FOCUS_ITEM );
		}

		@Override
		public void select( ToolTab tab ) {
			final int count = getItemCount();

			for( int index = 0; index < count; index++ ) {
				final ToolTab value = getModelItem( index );
				if( value != null && value.equals( tab ) ) {
					select( index );
					return;
				}
			}
		}

		private ToolTab findNearestAvailableTab( int tabIndex ) {
			// Try to select the nearest, non-disabled tab from the position of the closed tab
			int index = 1;
			ToolTab next = null;
			int count = getItemCount();

			while( true ) {
				// Look left
				int leftPosition = tabIndex - index;
				if( leftPosition >= 0 ) {
					ToolTab tab = getModelItem( leftPosition );
					if( tab != null && !tab.isDisable() ) {
						next = tab;
						break;
					}
				}

				// Look right
				int rightPosition = tabIndex + index - 1;
				if( rightPosition < count ) {
					ToolTab tab = getModelItem( rightPosition );
					if( tab != null && !tab.isDisable() ) {
						next = tab;
						break;
					}
				}

				if( leftPosition < 0 && rightPosition >= count ) break;
				index++;
			}

			return next;
		}

	}

}
