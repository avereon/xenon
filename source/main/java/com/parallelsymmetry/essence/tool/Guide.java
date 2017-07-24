package com.parallelsymmetry.essence.tool;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

public class Guide {

	public static final String GUIDE_KEY = GuideTool.class.getName() + ":guide";

	private TreeItem root;

	private SelectionMode selectionMode;

	public Guide() {
		selectionMode = SelectionMode.SINGLE;
	}

	public TreeItem getRoot() {
		return root;
	}

	public void setRoot( TreeItem root ) {
		this.root = root;
	}

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode( SelectionMode selectionMode ) {
		this.selectionMode = selectionMode == null ? SelectionMode.SINGLE : selectionMode;
	}

}
