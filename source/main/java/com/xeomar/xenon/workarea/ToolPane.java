package com.xeomar.xenon.workarea;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * The ToolPane class provides a custom tab pane component for the program
 * allowing for extended and custom capabilities.
 */
public class ToolPane extends BorderPane {

	private ToolHeader header;

	private SelectionModel selectionModel;

	public ToolPane() {
		selectionModel = new ToolPaneSelectionModel( this );

		// Create components
		header = new ToolHeader();

		// Organize components
		setTop( header );
	}

	public ObservableList<ToolTab> getTabs() {
		return header.getTabs();
	}

	public SelectionModel<ToolTab> getSelectionModel() {
		return selectionModel;
	}

	void setTool( Tool tool ) {
		getChildren().clear();
		if( tool != null ) setCenter( tool );
	}

	private class ToolHeader extends HBox {

		// Contains the ToolTab instances and keeps them in order
		private ObservableList<ToolTab> tabs;

		ToolHeader() {
			tabs = new SimpleListProperty<>();
		}

		ObservableList<ToolTab> getTabs() {
			return tabs;
		}

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
							this.findNearestAvailableTab( fromIndex, true );
						}
					}

					if( (change.wasAdded() || change.wasRemoved()) && this.getSelectedIndex() != pane.getTabs().indexOf( this.getSelectedItem() ) ) {
						this.clearAndSelect( pane.getTabs().indexOf( this.getSelectedItem() ) );
					}
				}

				if( this.getSelectedIndex() == -1 && this.getSelectedItem() == null && pane.getTabs().size() > 0 ) {
					this.findNearestAvailableTab( 0, true );
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
			ObservableList<ToolTab> list = pane.getTabs();
			return list == null ? 0 : list.size();
		}

		void findNearestAvailableTab( int index, boolean what ) {
			// TODO Implement findNearestAvailableTab()
		}

	}

}
