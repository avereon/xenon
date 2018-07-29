package com.xeomar.xenon.workarea;

import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.Tab;

public class DndTab extends Tab {

	public DndTab() {
		this( null );
	}

	public DndTab( String text ) {
		this( text, null );
	}

	public DndTab( String text, Node content ) {
		super( text, content );
		System.out.println( "New DndTab: " + text );
	}

	/** {@inheritDoc} */
	@Override
	public EventDispatchChain buildEventDispatchChain( EventDispatchChain tail ) {
		System.out.println( "DndTab.buildEventDispatchChain()..." );
		return tail.prepend( eventHandlerManager );
	}

	private final EventDispatcher eventHandlerManager = ( event, tail ) -> {
		System.out.println( "dispatchEvent: " + event );

		return null;
	};

}
