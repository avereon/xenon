package com.avereon.xenon.workspace;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.ui.util.MenuFactory;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.CustomLog;

@CustomLog
public class WorkareaMenu extends MenuBar {

	private final Menu menu;

	private final SeparatorMenuItem workareaSeparator;

	public WorkareaMenu( Xenon program, Workspace workspace ) {
		getStyleClass().addAll( "workarea-menu-bar" );
		menu = MenuFactory.createMenu( program, "workarea", true );
		getMenus().add( menu );

		// Link the active workarea property to the menu
		workspace.activeWorkareaProperty().addListener( ( p, o, n ) -> {
			if( n == null ) {
				menu.graphicProperty().unbind();
				menu.textProperty().unbind();
			} else {
				// FIXME This steals the icon from the node that has it
				menu.graphicProperty().bind( n.iconProperty() );
				menu.textProperty().bind( n.nameProperty() );
			}
		} );

		// Create the workarea action menu items
		MenuItem create = MenuFactory.createMenuItem( program, "workarea-new" );
		MenuItem rename = MenuFactory.createMenuItem( program, "workarea-rename" );
		MenuItem close = MenuFactory.createMenuItem( program, "workarea-close" );
		workareaSeparator = new SeparatorMenuItem();

		// Add the workarea action menu items
		menu.getItems().addAll( create, rename, close, workareaSeparator );

		// Update the workarea menu when the workareas change
		workspace.workareasProperty().addListener( (ListChangeListener<Workarea>)c -> {
			int startIndex = menu.getItems().indexOf( workareaSeparator );
			if( startIndex < 0 ) return;

			// Remove existing workarea menu items
			menu.getItems().remove( startIndex+1, menu.getItems().size() );

			// Update the list of workarea menu items
			menu.getItems().addAll( c.getList().stream().map( WorkareaMenuItem::new ).toList() );
		});

	}

}
