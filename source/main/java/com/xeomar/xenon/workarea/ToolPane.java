package com.xeomar.xenon.workarea;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class ToolPane extends TabPane {

	private Map<String, DndTab> tabIdMap;

	ToolPane() {
		this( (DndTab[])null );
	}

	private ToolPane( DndTab... tabs ) {
		super( tabs );
		tabIdMap = new ConcurrentHashMap<>();

		getTabs().addListener( (ListChangeListener<Tab>)listener -> {
			while( listener.next() ) {
				for( Tab tab : listener.getRemoved() ) {
					if( tab != null && !getTabs().contains( tab ) ) tabIdMap.remove( tab );
				}

				for( Tab tab : listener.getAddedSubList() ) {
					if( tab != null ) tabIdMap.put( tab.getId(), (DndTab)tab );
				}
			}
		} );

		if( tabs != null ) {
			for( DndTab tab : tabs ) {
				tabIdMap.put( tab.getId(), tab );
			}
		}

		// Add mouse listeners to handle drag and drop
		setOnMousePressed( event -> Objects.requireNonNull( getTab( event ) ).getDndSupport().mousePressed( event ) );
		setOnMouseDragged( event -> Objects.requireNonNull( getTab( event ) ).getDndSupport().mouseDragged( event ) );
		setOnMouseReleased( event -> Objects.requireNonNull( getTab( event ) ).getDndSupport().mouseReleased( event ) );

		// TODO Make this a user setting
		setTabClosingPolicy( TabPane.TabClosingPolicy.ALL_TABS );
	}

	private DndTab getTab( MouseEvent event ) {
		Node node = (Node)event.getTarget();

		while( node != null ) {
			if( node.getStyleClass().contains( "tab" ) ) return tabIdMap.get( node.getId() );
			node = node.getParent();
		}

		return null;
	}

}
