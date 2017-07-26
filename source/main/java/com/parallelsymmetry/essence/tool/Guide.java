package com.parallelsymmetry.essence.tool;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

public class Guide<T> {

	public static final String GUIDE_KEY = GuideTool.class.getName() + ":guide";

	private TreeItem<T> root;

	private SelectionMode selectionMode;

	private BooleanProperty activeProperty;

	private ReadOnlyObjectWrapper<TreeItem<T>> selectedItem = new ReadOnlyObjectWrapper<TreeItem<T>>( this, "selectedItem" );

	public Guide() {
		selectionMode = SelectionMode.SINGLE;
		activeProperty = new SimpleBooleanProperty( false );
		selectedItem = new ReadOnlyObjectWrapper<TreeItem<T>>( this, "selectedItem" );
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

	public boolean isActive() {
		return activeProperty.get();
	}

	public void setActive( boolean active ) {
		activeProperty.set( active );
	}

	public BooleanProperty activeProperty() {
		return activeProperty;
	}

	public final ReadOnlyObjectProperty<TreeItem<T>> selectedItemProperty() {
		return selectedItem.getReadOnlyProperty();
	}

	final void setSelectedItem( TreeItem<T> value ) { selectedItem.set( value ); }
}
