package com.xeomar.xenon.workarea;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
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

		//		private Node tabSkin;
		//
		//		public Node getTabSkin() {
		//			return tabSkin;
		//		}
		//
		//		public void setTabSkin( Node tabSkin ) {
		//			this.tabSkin = tabSkin;
		//		}

		private Image dragImage;

		public void mousePressed( MouseEvent event ) {
			System.out.println( "Moused pressed: " + event );
			// NEXT Collect original event coordinates
		}

		public void mouseDragged( MouseEvent event ) {
			//System.out.println( "Moused dragged: " + event );
			// NEXT Generate node for dragging
			if( dragImage == null ) {
				Node node = getTabSkin(event );
				int width = (int)node.getBoundsInParent().getWidth();
				int height = (int)node.getBoundsInParent().getHeight();
				dragImage = node.snapshot( new SnapshotParameters(), new WritableImage( width, height ) );
			}



		}

		public void mouseReleased( MouseEvent event ) {
			System.out.println( "Moused released: " + event );

			// Remove DndSupport
			dndSupport = null;
		}

		private Node getTabSkin( MouseEvent event ) {
			Node node = (Node)event.getTarget();

			while( node != null ) {
				if( node.getStyleClass().contains( "tab" ) ) return node;
				node = node.getParent();
			}

			return null;
		}

	}

}
