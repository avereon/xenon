package com.xeomar.xenon.workarea;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;

import java.util.UUID;

public class DndTab extends Tab {

	private DndSupport dndSupport;

	public DndTab() {
		this( null );
	}

	public DndTab( String text ) {
		this( text, null );
	}

	public DndTab( String text, Node content ) {
		super( text, content );
		setId( UUID.randomUUID().toString() );
	}

	public DndSupport getDndSupport() {
		if( dndSupport == null ) dndSupport = new DndSupport();
		return dndSupport;
	}

	class DndSupport {

		private Node tabSkin;

		public Node getTabSkin() {
			return tabSkin;
		}

		public void setTabSkin( Node tabSkin ) {
			this.tabSkin = tabSkin;
		}

		public void mousePressed( MouseEvent event ) {
			System.out.println( "Moused pressed: " + event );
			// NEXT Collect original event coordinates
		}

		public void mouseDragged( MouseEvent event ) {
			System.out.println( "Moused dragged: " + event );
			// NEXT Generate node for dragging
		}

		public void mouseReleased( MouseEvent event ) {
			System.out.println( "Moused released: " + event );

			// Remove DndSupport
			dndSupport = null;
		}

	}

}
