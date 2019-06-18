package com.xeomar.xenon.workarea;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The ToolPane class provides a custom tab pane component for the program
 * allowing for extended and custom capabilities.
 */
public class ToolPane extends BorderPane {

	private ObjectProperty<SingleSelectionModel<ToolTab>> selectionModel = new SimpleObjectProperty<>( this, "selectionModel" );

	private HBox header;

	private ObservableList<ToolTab> tabs = FXCollections.observableList( new CopyOnWriteArrayList<>(  ) );

	public ToolPane() {
		getStyleClass().setAll( "tool-tab-pane" );
		setSelectionModel( new ToolPaneSelectionModel( this ) );

		// Create components
		header = new HBox();
		header.getStyleClass().setAll( "tool-tab-pane-header" );

		// Organize components
		setTop( header );

		this.tabs.addListener( (ListChangeListener<ToolTab>)change -> {
			while( change.next() ) {
				for( ToolTab tab : change.getRemoved() ) {
					if( tab != null && !getTabs().contains( tab ) ) tab.setToolPane( null );
					header.getChildren().remove( tab );
				}

				for( ToolTab tab : change.getAddedSubList() ) {
					if( tab != null ) tab.setToolPane( ToolPane.this );
				}

				if( change.wasRemoved() ) header.getChildren().removeAll( change.getRemoved() );
				if( change.wasAdded() ) header.getChildren().addAll( change.getFrom(), change.getAddedSubList() );
			}
		} );
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

	private void setTool( Tool tool ) {
		if( tool != null ) setCenter( tool );
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
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) pane.getTabs().get( getSelectedIndex() ).setSelected( false );

			setSelectedIndex( index );
			ToolTab tab = getModelItem( index );
			if( tab != null ) {
				setSelectedItem( tab );
				pane.setTool( tab.getTool() );
			}

			// Select the new tab
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) pane.getTabs().get( getSelectedIndex() ).setSelected( true );

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
