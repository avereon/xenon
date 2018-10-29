package com.xeomar.xenon.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class FxUtil {

	public static Pos parseAlign( String align ) {
		switch( align ) {
			case "northwest" : return Pos.TOP_LEFT;
			case "north" : return Pos.TOP_CENTER;
			case "northeast" : return Pos.TOP_RIGHT;
			case "west" : return Pos.CENTER_LEFT;
			case "center" : return Pos.CENTER;
			case "east" : return Pos.CENTER_RIGHT;
			case "southwest" : return Pos.BOTTOM_LEFT;
			case "south" : return Pos.BOTTOM_CENTER;
			case "southeast" : return Pos.BOTTOM_RIGHT;
		}
		return Pos.CENTER;
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
		if( !Platform.isFxApplicationThread() ) throw new IllegalStateException( "Not on FX application thread; currentThread = " + Thread.currentThread().getName() );
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
