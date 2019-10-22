package com.avereon.xenon.workarea;

import com.avereon.util.LogUtil;
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
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * The ToolPane class provides a custom tab pane component for the program
 * allowing for extended and custom capabilities.
 */
public class ToolPane extends Control {

	static final PseudoClass ACTIVE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "active" );

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ObjectProperty<SingleSelectionModel<ToolTab>> selectionModel = new SimpleObjectProperty<>( this, "selectionModel" );

	private ObservableList<ToolTab> tabs = FXCollections.observableArrayList();

	private ReadOnlyBooleanWrapper active;

	public ToolPane() {
		getStyleClass().addAll( "tool-pane" );
		setSelectionModel( new ToolPaneSelectionModel( this ) );
	}

	public ObservableList<ToolTab> getTabs() {
		return tabs;
	}

	public final SingleSelectionModel<ToolTab> getSelectionModel() {
		return selectionModel.get();
	}

	public final void setSelectionModel( SingleSelectionModel<ToolTab> value ) {
		selectionModel.set( value );
	}

	public boolean isActive() {
		return active != null && active.get();
	}

	public void setActive( boolean active ) {
		activePropertyImpl().set( active );
	}

	public ReadOnlyBooleanWrapper activeProperty() {
		return activePropertyImpl();
	}

	private ReadOnlyBooleanWrapper activePropertyImpl() {
		if( active == null ) {
			active = new ReadOnlyBooleanWrapper() {

				@Override
				protected void invalidated() {
					log.info( "Toolpane active: " + isActive() );
					pseudoClassStateChanged( ACTIVE_PSEUDOCLASS_STATE, isActive() );
					//if( getOnActivatedChanged() != null ) Event.fireEvent( ToolTab.this, new Event( ACTIVATED_CHANGED_EVENT ) );
				}

				@Override
				public Object getBean() {
					return ToolPane.this;
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
	protected Skin<ToolPane> createDefaultSkin() {
		return new ToolPaneSkin( this );
	}

	protected WorkpaneView getWorkpaneView() {
		return (WorkpaneView)getParent();
	}

	protected Workpane getWorkpane() {
		return getWorkpaneView().getWorkpane();
	}

	void handleDrop( DragEvent event, int index, Side side ) {
		// NOTE If the event gesture source is null the drag came from outside the program

		try {
			boolean droppedOnArea = index == -2;
			Tool sourceTool = ((ToolTab)event.getGestureSource()).getTool();
			Workpane sourcePane = sourceTool.getWorkpane();
			WorkpaneView targetView = getWorkpaneView();
			Workpane targetPane = getWorkpane();

			log.debug( "DnD transfer mode: " + event.getTransferMode() );

			if( event.getTransferMode() == TransferMode.MOVE ) {
				if( droppedOnArea && sourceTool == targetView.getActiveTool() ) return;
				sourcePane.removeTool( sourceTool );
			} else if( event.getTransferMode() == TransferMode.COPY ) {
				log.warn( "Tool copy not implemented yet!");
				sourceTool = cloneTool( sourceTool );
			}

			if( side != null ) targetView = targetPane.split( targetView, side );

			int targetViewTabCount = targetView.getTools().size();
			if( index < 0 || index > targetViewTabCount ) index = targetViewTabCount;

			targetPane.addTool( sourceTool, targetView, index, true );
			sourcePane.setDropHint( null );
		} finally {
			event.setDropCompleted( true );
			event.consume();
		}
	}

	private Tool cloneTool( Tool tool ) {
		// TODO Implement tool cloning
		return tool;
	}

	private static class ToolPaneSelectionModel extends SingleSelectionModel<ToolTab> {

		private ToolPane pane;

		ToolPaneSelectionModel( ToolPane pane ) {
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

				if( this.getSelectedIndex() == -1 && this.getSelectedItem() == null && pane.getTabs().size() > 0 ) {
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
			if( index < 0 || (getItemCount() > 0 && index >= getItemCount()) || (index == getSelectedIndex() && getModelItem( index ).isSelected()) ) {
				return;
			}

			// Unselect the old tab
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) {
				pane.getTabs().get( getSelectedIndex() ).setSelected( false );
			}

			setSelectedIndex( index );
			ToolTab tab = getModelItem( index );
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
