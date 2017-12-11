package com.xeomar.xenon.util;

import javafx.scene.Node;

public class UiUtil {

	public static boolean isChildOf( Node node, Node container ) {
		while( (node = node.getParent()) != null ) {
			if( node == container ) return true;
		}
		return false;
	}

}
