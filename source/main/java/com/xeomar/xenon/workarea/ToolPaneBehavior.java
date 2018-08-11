package com.xeomar.xenon.workarea;

import javafx.event.Event;
import javafx.scene.control.SelectionModel;

import java.util.List;

public class ToolPaneBehavior {

	private ToolPane node;

	public ToolPaneBehavior( ToolPane toolPane ) {
		this.node = toolPane;
	}

	private ToolPane getNode() {
		return node;
	}

	public void selectTab( ToolTab tab ) {
		getNode().getSelectionModel().select( tab );
	}

	public boolean canCloseTab( ToolTab tab ) {
		Event event = new Event( tab, tab, ToolTab.TAB_CLOSE_REQUEST_EVENT );
		Event.fireEvent( tab, event );
		return !event.isConsumed();
	}

	public void closeTab( ToolTab tab ) {
		ToolPane tabPane = getNode();
		// only switch to another tab if the selected tab is the one we're closing
		int index = tabPane.getTabs().indexOf( tab );
		if( index != -1 ) {
			tabPane.getTabs().remove( index );
		}
		if( tab.getOnClosed() != null ) Event.fireEvent( tab, new Event( ToolTab.CLOSED_EVENT ) );
	}

	// Find a tab after the currently selected that is not disabled. Loop around
	// if no tabs are found after currently selected tab.
	public void selectNextTab() {
		moveSelection( 1 );
	}

	// Find a tab before the currently selected that is not disabled.
	public void selectPreviousTab() {
		moveSelection( -1 );
	}

	private void moveSelection( int delta ) {
		moveSelection( getNode().getSelectionModel().getSelectedIndex(), delta );
	}

	private void moveSelection( int startIndex, int delta ) {
		final ToolPane tabPane = getNode();
		if( tabPane.getTabs().isEmpty() ) return;

		int tabIndex = findValidTab( startIndex, delta );
		if( tabIndex > -1 ) {
			final SelectionModel<ToolTab> selectionModel = tabPane.getSelectionModel();
			selectionModel.select( tabIndex );
		}
		tabPane.requestFocus();
	}

	private int findValidTab( int startIndex, int delta ) {
		final ToolPane tabPane = getNode();
		final List<ToolTab> tabs = tabPane.getTabs();
		final int max = tabs.size();

		int index = startIndex;
		do {
			index = nextIndex( index + delta, max );
			ToolTab tab = tabs.get( index );
			if( tab != null && !tab.isDisable() ) return index;
		} while( index != startIndex );

		return -1;
	}

	private int nextIndex( int value, int max ) {
		final int min = 0;
		int result = value % max;
		if( result > min && max < min ) {
			return result + max - min;
		} else if( result < min && max > min ) {
			return result + max - min;
		}
		return result;
	}

}
