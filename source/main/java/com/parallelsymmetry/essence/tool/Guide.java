package com.parallelsymmetry.essence.tool;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

public class Guide<T> {

	public static final String GUIDE_KEY = GuideTool.class.getName() + ":guide";

	private TreeItem<T> root;

	private SelectionMode selectionMode;

	private ReadOnlyObjectWrapper<TreeItem<T>> selectedItem = new ReadOnlyObjectWrapper<TreeItem<T>>( this, "selectedItem" );

	public Guide() {
		selectionMode = SelectionMode.SINGLE;
	}

	public TreeItem<T> getRoot() {
		return root;
	}

	public void setRoot( TreeItem<T> root ) {
		this.root = root;
	}

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode( SelectionMode selectionMode ) {
		this.selectionMode = selectionMode == null ? SelectionMode.SINGLE : selectionMode;
	}

	public final ReadOnlyObjectProperty<TreeItem<T>> selectedItemProperty() {
		return selectedItem.getReadOnlyProperty();
	}

	final void setSelectedItem( TreeItem<T> value ) { selectedItem.set( value ); }
}
