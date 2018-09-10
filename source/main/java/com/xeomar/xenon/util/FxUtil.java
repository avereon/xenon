package com.xeomar.xenon.util;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

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

}
