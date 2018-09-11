package com.xeomar.xenon.tool.guide;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

public class Guide {

	public static final String GUIDE_KEY = GuideTool.class.getName() + ":guide";

	private TreeItem<GuideNode> root;

	private SelectionMode selectionMode;

	private BooleanProperty activeProperty;

	@Deprecated
	private ReadOnlyObjectWrapper<TreeItem<GuideNode>> selectedItem;

	// NOTE Should the selected item model use the TreeItem(not encouraged),
	// the GuideNode(possibly a good option) or the GuideNode id(good option
	// for storing in settings). Either way the code must be able to restore
	// the selected state from just ids since they will be stored in settings.
	// But using just ids may not perform well and TreeItems are ultimately
	// needed for the TreeView in the GuideTool.
	//
	// It turns out that multiple selections are even worse because the
	// TreeView will only take indicies.

	public Guide() {
		this.root = new TreeItem<>( new GuideNode() );
		activeProperty = new SimpleBooleanProperty( false );
		selectedItem = new ReadOnlyObjectWrapper<>( this, "selectedItem" );
		setSelectionMode( SelectionMode.SINGLE );
	}

	public TreeItem<GuideNode> getRoot() {
		return root;
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

	/* Only intended to be used by the GuideTool */
	@Deprecated
	final ReadOnlyObjectProperty<TreeItem<GuideNode>> selectedItemProperty() {
		return selectedItem.getReadOnlyProperty();
	}

	@Deprecated
	final void setSelectedItem( TreeItem<GuideNode> value ) {
		selectedItem.set( value );
	}

	@Deprecated
	protected final void setSelected( String id ) {
		TreeItem<GuideNode> node = findItem( getRoot(), id );
		if( node != null ) setSelectedItem( node );
	}

	private TreeItem<GuideNode> findItem( TreeItem<GuideNode> node, String id ) {
		if( node == null || id == null ) return null;
		if( node != root && node.getValue().getId().equals( id ) ) return node;

		for( TreeItem<GuideNode> child : node.getChildren() ) {
			TreeItem<GuideNode> check = findItem( child, id );
			if( check != null ) return check;
		}

		return null;
	}

}
