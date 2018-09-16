package com.xeomar.xenon.tool.guide;

import com.xeomar.xenon.util.FxUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

import java.util.*;

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

	private ReadOnlyStringWrapper selectedId;

	private ReadOnlyListWrapper<TreeItem<GuideNode>> selectedItems;

	public Guide() {
		this.root = new TreeItem<>( new GuideNode() );
		activeProperty = new SimpleBooleanProperty( false );
		selectedItem = new ReadOnlyObjectWrapper<>( this, "selectedItem" );
		selectedItems = new ReadOnlyListWrapper<>( this, "selectedItems", FXCollections.observableArrayList() );
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

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setExpandedIds( String... ids ) {
		Set<String> idSet = Set.of( ids );
		for( TreeItem<GuideNode> item : FxUtil.flatTree( root ) ) {
			if( item == root ) continue;
			item.setExpanded( idSet.contains( item.getValue().getId() ) );
		}
	}

	final Set<String> getExpandedIds() {
		Set<String> idSet = new HashSet<>();
		for( TreeItem<GuideNode> item : FxUtil.flatTree( root ) ) {
			if( item == root ) continue;
			if( item.isExpanded() ) idSet.add( item.getValue().getId() );
		}
		return idSet;
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final ReadOnlyListProperty<TreeItem<GuideNode>> selectedItemsProperty() {
		return selectedItems.getReadOnlyProperty();
	}

	final List<TreeItem<GuideNode>> getSelectedItems() {
		return Collections.unmodifiableList( selectedItems.get() );
	}

	final void setSelectedItems( List<TreeItem<GuideNode>> items ) {
		selectedItems.setAll( items );
	}

	final List<String> getSelectedIds() {
		List<String> ids = new ArrayList<>( selectedItems.size() );

		for( TreeItem<GuideNode> item : getSelectedItems() ) {
			ids.add( item.getValue().getId() );
		}

		return ids;
	}

	final void setSelectedIds( String... ids ) {
		Map<String, TreeItem<GuideNode>> itemMap = getItemMap();

		List<TreeItem<GuideNode>> newItems = new ArrayList<>( ids.length );
		for( String id : ids ) {
			TreeItem<GuideNode> item = itemMap.get( id );
			if( item != null ) newItems.add( item );
		}

		setSelectedItems( newItems );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
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
		TreeItem<GuideNode> node = findItem( id );
		if( node != null ) setSelectedItem( node );
	}

	private Map<String, TreeItem<GuideNode>> getItemMap() {
		Map<String, TreeItem<GuideNode>> itemMap = new HashMap<>();
		for( TreeItem<GuideNode> item : FxUtil.flatTree( root ) ) {
			if( item == root ) continue;
			itemMap.put( item.getValue().getId(), item );
		}
		return itemMap;
	}

	private TreeItem<GuideNode> findItem( String id ) {
		return findItem( root, id );
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
