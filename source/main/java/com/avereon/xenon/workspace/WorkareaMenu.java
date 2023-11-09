package com.avereon.xenon.workspace;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.ui.util.MenuFactory;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.CustomLog;

@CustomLog
public class WorkareaMenu {

	public static MenuButton createWorkareaMenu(Xenon program, Workspace workspace) {
		MenuButton menu = MenuFactory.createMenuButton( program, "workarea", true );
		menu.getStyleClass().addAll( "workarea-menu" );

		//menu.getStyleClass().addAll( "workarea-menu-bar" );
		//menu = MenuFactory.createMenu( program, "workarea", false );
		//getItems().add( menu );

		// Link the active workarea property to the menu
		workspace.activeWorkareaProperty().addListener( ( p, o, n ) -> {
			if( n == null ) {
				menu.graphicProperty().unbind();
				menu.textProperty().unbind();
			} else {
				menu.graphicProperty().bind( n.iconProperty().map(i -> program.getIconLibrary().getIcon( i )) );
				menu.textProperty().bind( n.nameProperty() );
			}
		} );

		// Create the workarea action menu items
		MenuItem create = MenuFactory.createMenuItem( program, "workarea-new" );
		MenuItem rename = MenuFactory.createMenuItem( program, "workarea-rename" );
		MenuItem close = MenuFactory.createMenuItem( program, "workarea-close" );
		SeparatorMenuItem workareaSeparator = new SeparatorMenuItem();

		// Add the workarea action menu items
		menu.getItems().addAll( create, rename, close, workareaSeparator );

		// Update the workarea menu when the workareas change
		workspace.workareasProperty().addListener( (ListChangeListener<Workarea>)c -> {
			int startIndex = menu.getItems().indexOf( workareaSeparator );
			if( startIndex < 0 ) return;

			// Remove existing workarea menu items
			menu.getItems().remove( startIndex+1, menu.getItems().size() );

			// Update the list of workarea menu items
			menu.getItems().addAll( c.getList().stream().map( WorkareaMenu::createWorkareaMenuItem2 ).toList() );
		});

		return menu;
	}

	private static WorkareaMenuItem createWorkareaMenuItem( Workarea workarea ) {
		WorkareaMenuItem item = new WorkareaMenuItem( workarea );

		//item.setOnAction( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
		item.getStyleableNode().setOnMousePressed( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );

		return item;
	}

	private static MenuItem createWorkareaMenuItem2( Workarea workarea ) {
		MenuItem item  = new MenuItem();
		item.textProperty().bind( workarea.nameProperty() );
		item.graphicProperty().bind( workarea.iconProperty().map( i -> workarea.getProgram().getIconLibrary().getIcon( i )));
		item.getStyleClass().addAll("workarea-menu-item");
		item.setOnAction( e -> workarea.getWorkspace().setActiveWorkarea( workarea ) );
		return item;
	}

}
