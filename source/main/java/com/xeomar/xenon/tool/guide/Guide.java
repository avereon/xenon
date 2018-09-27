package com.xeomar.xenon.tool.guide;

import com.xeomar.xenon.util.FxUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

import java.util.*;

public class Guide {

	public static final String GUIDE_KEY = GuideTool.class.getName() + ":guide";

	private TreeItem<GuideNode> root;

	private SelectionMode selectionMode;

	private BooleanProperty activeProperty;

	private ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> selectedItems;

	public Guide() {
		this.root = new TreeItem<>( new GuideNode() );
		activeProperty = new SimpleBooleanProperty( false );
		selectedItems = new ReadOnlyObjectWrapper<>( this, "selectedItems", new HashSet<>() );
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
	final ReadOnlyObjectProperty<Set<TreeItem<GuideNode>>> selectedItemsProperty() {
		return selectedItems.getReadOnlyProperty();
	}

	final void setSelectedItems( Set<TreeItem<GuideNode>> items ) {
		selectedItems.set( items );
	}

	final List<String> getSelectedIds() {
		Set<TreeItem<GuideNode>> selectedItems = Collections.unmodifiableSet( this.selectedItems.get() );

		List<String> ids = new ArrayList<>( selectedItems.size() );
		for( TreeItem<GuideNode> item : selectedItems ) {
			ids.add( item.getValue().getId() );
		}

		return ids;
	}

	final void setSelectedIds( String... ids ) {
		Map<String, TreeItem<GuideNode>> itemMap = getItemMap();

		Set<TreeItem<GuideNode>> newItems = new HashSet<>( ids.length );
		for( String id : ids ) {
			TreeItem<GuideNode> item = itemMap.get( id );
			if( item != null ) newItems.add( item );
		}

		setSelectedItems( newItems );
	}

	protected final GuideNode getNode( String id ) {
		TreeItem item = findItem( id );
		return item == null ? null : (GuideNode)item.getValue();
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

	static String itemsToString( Set<? extends TreeItem<GuideNode>> nodes ) {
		if( nodes == null ) return null;
		if( nodes.size() == 0 ) return "";

		StringBuilder builder = new StringBuilder();
		for( TreeItem<GuideNode> node : nodes ) {
			builder.append( node.getValue().getId() ).append( "," );
		}

		String ids = builder.toString();
		ids = ids.substring( 0, ids.length() - 1 );
		return ids;
	}

}
