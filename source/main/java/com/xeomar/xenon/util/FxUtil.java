package com.xeomar.xenon.util;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class FxUtil {

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

}
