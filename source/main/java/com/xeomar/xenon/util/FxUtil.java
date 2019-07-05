package com.xeomar.xenon.util;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BackgroundPosition;

import java.util.ArrayList;
import java.util.List;

public class FxUtil {

	public static Pos parseAlign( String align ) {
		switch( align ) {
			case "northwest":
				return Pos.TOP_LEFT;
			case "north":
				return Pos.TOP_CENTER;
			case "northeast":
				return Pos.TOP_RIGHT;
			case "west":
				return Pos.CENTER_LEFT;
			case "center":
				return Pos.CENTER;
			case "east":
				return Pos.CENTER_RIGHT;
			case "southwest":
				return Pos.BOTTOM_LEFT;
			case "south":
				return Pos.BOTTOM_CENTER;
			case "southeast":
				return Pos.BOTTOM_RIGHT;
		}
		return Pos.CENTER;
	}

	public static BackgroundPosition parseBackgroundPosition( String align ) {
		switch( align ) {
			case "northwest":
				return new BackgroundPosition( Side.LEFT, 0, true, Side.TOP, 0, true );
			case "north":
				return new BackgroundPosition( Side.LEFT, 0.5, true, Side.TOP, 0, true );
			case "northeast":
				return new BackgroundPosition( Side.LEFT, 1, true, Side.TOP, 0, true );
			case "west":
				return new BackgroundPosition( Side.LEFT, 0, true, Side.TOP, 0.5, true );
			case "center":
				return new BackgroundPosition( Side.LEFT, 0.5, true, Side.TOP, 0.5, true );
			case "east":
				return new BackgroundPosition( Side.LEFT, 1, true, Side.TOP, 0.5, true );
			case "southwest":
				return new BackgroundPosition( Side.LEFT, 0, true, Side.TOP, 1, true );
			case "south":
				return new BackgroundPosition( Side.LEFT, 0.5, true, Side.TOP, 1, true );
			case "southeast":
				return new BackgroundPosition( Side.LEFT, 1, true, Side.TOP, 1, true );
		}
		return BackgroundPosition.CENTER;
	}

	public static boolean isChildOf( Node node, Node container ) {
		while( (node = node.getParent()) != null ) {
			if( node == container ) return true;
		}
		return false;
	}

	public static boolean isParentOf( TreeItem item, TreeItem child ) {
		TreeItem parent = child;

		while( parent != null ) {
			if( item.equals( parent ) ) return true;
			parent = parent.getParent();
		}

		return false;
	}

	public static Bounds localToParent( Node source, Node target ) {
		return localToParent( source, target, source.getLayoutBounds() );
	}

	public static Bounds localToParent( Node source, Node target, Bounds bounds ) {
		Bounds result = bounds;

		Node parent = source;
		while( parent != null ) {
			if( parent == target ) break;
			result = parent.localToParent( result );
			parent = parent.getParent();
		}

		return result;
	}

	public static Insets add( Insets a, Insets b ) {
		return new Insets( a.getTop() + b.getTop(), a.getRight() + b.getRight(), a.getBottom() + b.getBottom(), a.getLeft() + b.getLeft() );
	}

	public static Bounds add( Bounds a, Insets b ) {
		return new BoundingBox( a.getMinX(), a.getMinY(), a.getWidth() + b.getLeft() + b.getRight(), a.getHeight() + b.getTop() + b.getBottom() );
	}

	public static <T> List<TreeItem<T>> flatTree( TreeItem<T> item ) {
		return flatTree( item, false );
	}

	public static <T> List<TreeItem<T>> flatTree( TreeItem<T> item, boolean includeItem ) {
		List<TreeItem<T>> list = new ArrayList<>();

		if( includeItem ) list.add( item );
		item.getChildren().forEach( ( child ) -> list.addAll( flatTree( child, true ) ) );

		return list;
	}

	public static void checkFxUserThread() {
		if( !Platform.isFxApplicationThread() ) {
			throw new IllegalStateException( "Not on FX application thread; currentThread = " + Thread.currentThread().getName() );
		}
	}

	public static void fxWait( long timeout ) throws InterruptedException {
		WaitToken token = new WaitToken();
		Platform.runLater( token );
		token.fxWait( timeout );
	}

	private static class WaitToken implements Runnable {

		boolean released;

		public synchronized void run() {
			this.released = true;
			this.notifyAll();
		}

		public synchronized void fxWait( long timeout ) throws InterruptedException {
			while( !released ) {
				wait( timeout );
			}
		}

	}

}
