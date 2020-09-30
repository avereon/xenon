package com.avereon.xenon.tool.guide;

import com.avereon.util.Log;
import com.avereon.zerra.javafx.Fx;
import com.avereon.zerra.javafx.FxUtil;
import javafx.beans.property.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;

import java.lang.System.Logger;
import java.util.*;
import java.util.stream.Collectors;

public class Guide {

	public static final Guide EMPTY = new Guide();

	private static final Logger log = Log.get();

	private final TreeItem<GuideNode> root;

	private SelectionMode selectionMode;

	private final StringProperty titleProperty;

	private final StringProperty iconProperty;

	private final BooleanProperty activeProperty;

	private final BooleanProperty dragAndDropEnabledProperty;

	private final ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> expandedItems;

	private final ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> selectedItems;

	public Guide() {
		this.root = new TreeItem<>();
		titleProperty = new SimpleStringProperty();
		iconProperty = new SimpleStringProperty();
		activeProperty = new SimpleBooleanProperty( false );
		dragAndDropEnabledProperty = new SimpleBooleanProperty( false );
		expandedItems = new ReadOnlyObjectWrapper<>( this, "expandedItems", new HashSet<>() );
		selectedItems = new ReadOnlyObjectWrapper<>( this, "selectedItems", new HashSet<>() );
		root.addEventHandler( TreeItem.branchExpandedEvent(), ( event ) -> updateExpandedItems() );
		root.addEventHandler( TreeItem.branchCollapsedEvent(), ( event ) -> updateExpandedItems() );
		setSelectionMode( SelectionMode.SINGLE );
	}

	public String getTitle() {
		return titleProperty.get();
	}

	public Guide setTitle( String name ) {
		titleProperty.set( name );
		return this;
	}

	public StringProperty titleProperty() {
		return titleProperty;
	}

	public String getIcon() {
		return iconProperty.get();
	}

	public Guide setIcon( String icon ) {
		iconProperty.set( icon );
		return this;
	}

	public StringProperty iconProperty() {
		return iconProperty;
	}

	public final GuideNode getNode( String id ) {
		TreeItem<GuideNode> item = findItem( id );
		return item == null ? null : item.getValue();
	}

	public final GuideNode addNode( GuideNode node ) {
		return addNode( null, node );
	}

	public final GuideNode addNode( GuideNode parent, GuideNode node ) {
		Fx.run( () -> {
			TreeItem<GuideNode> item = parent == null ? root : parent.getTreeItem();
			item.getChildren().add( node.getTreeItem() );
		} );
		return node;
	}

	public final GuideNode removeNode( GuideNode node ) {
		Fx.run( () -> node.getTreeItem().getParent().getChildren().remove( node.getTreeItem() ) );
		return node;
	}

	public final Guide clear() {
		return clear( null );
	}

	public final Guide clear( GuideNode node ) {
		Fx.run( () -> {
			TreeItem<GuideNode> item = node == null ? root : node.getTreeItem();
			item.getChildren().clear();
		} );
		return this;
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

	public boolean isDragAndDropEnabled() {
		return dragAndDropEnabledProperty.get();
	}

	public void setDragAndDropEnabled( boolean enabled ) {
		dragAndDropEnabledProperty.set( enabled );
	}

	public BooleanProperty dragAndDropEnabledProperty() {
		return dragAndDropEnabledProperty;
	}

	/* Only intended to be used by the GuideTool */
	final TreeItem<GuideNode> getRoot() {
		return root;
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final Set<String> getExpandedIds() {
		Set<String> idSet = new HashSet<>();
		for( TreeItem<GuideNode> item : FxUtil.flatTree( root ) ) {
			if( item == root ) continue;
			if( item.isExpanded() ) idSet.add( item.getValue().getId() );
		}
		return idSet;
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setExpandedIds( Set<String> ids ) {
		FxUtil.assertFxThread();
		for( TreeItem<GuideNode> item : FxUtil.flatTree( root ) ) {
			if( item == root ) continue;
			item.setExpanded( ids.contains( item.getValue().getId() ) );
		}
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final ReadOnlyObjectProperty<Set<TreeItem<GuideNode>>> expandedItemsProperty() {
		return expandedItems.getReadOnlyProperty();
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setExpandedItems( Set<TreeItem<GuideNode>> items ) {
		FxUtil.assertFxThread();
		expandedItems.set( items );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final List<String> getSelectedIds() {
		Set<TreeItem<GuideNode>> selectedItems = Collections.unmodifiableSet( this.selectedItems.get() );

		List<String> ids = new ArrayList<>( selectedItems.size() );
		for( TreeItem<GuideNode> item : selectedItems ) {
			ids.add( item.getValue().getId() );
		}

		return ids;
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setSelectedIds( Set<String> ids ) {
		FxUtil.assertFxThread();
		Map<String, TreeItem<GuideNode>> itemMap = getItemMap();

		Set<TreeItem<GuideNode>> newItems = new HashSet<>( ids.size() );
		for( String id : ids ) {
			TreeItem<GuideNode> item = itemMap.get( id );
			if( item != null ) newItems.add( item );
		}

		setSelectedItems( newItems );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final ReadOnlyObjectProperty<Set<TreeItem<GuideNode>>> selectedItemsProperty() {
		return selectedItems.getReadOnlyProperty();
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setSelectedItems( Set<TreeItem<GuideNode>> items ) {
		FxUtil.assertFxThread();
		selectedItems.set( items );
	}

	protected void moveNode( GuideNode item, GuideNode target, boolean below, boolean child ) {
		//
	}

	private void updateExpandedItems() {
		setExpandedItems( FxUtil.flatTree( root ).stream().filter( (TreeItem::isExpanded) ).filter( ( item ) -> !item.isLeaf() ).collect( Collectors.toSet() ) );
	}

	private Map<String, TreeItem<GuideNode>> getItemMap() {
		return FxUtil.flatTree( root ).stream().collect( Collectors.toMap( item -> item.getValue().getId(), item -> item ) );
	}

	private TreeItem<GuideNode> findItem( GuideNode node ) {
		return findItem( node.getId() );
	}

	private TreeItem<GuideNode> findItem( String id ) {
		return findItem( root, id );
	}

	private TreeItem<GuideNode> findItem( GuideNode parent, GuideNode node ) {
		return findItem( node.getId() );
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

	/**
	 * Get a string of the tree item guide node ids.
	 * <p>
	 * Used for debugging.
	 *
	 * @param nodes The set of tree items
	 * @return A comma delimited string of the node ids
	 */
	@SuppressWarnings( "unused" )
	static String itemsToString( Set<? extends TreeItem<GuideNode>> nodes ) {
		return nodesToString( nodes.stream().map( TreeItem::getValue ).collect( Collectors.toSet() ) );
	}

	/**
	 * Get a string of the guide node ids.
	 * <p>
	 * Used for debugging.
	 *
	 * @param nodes The set of nodes
	 * @return A comma delimited string of the node ids
	 */
	@SuppressWarnings( "unused" )
	static String nodesToString( Set<GuideNode> nodes ) {
		if( nodes == null ) return null;
		if( nodes.size() == 0 ) return "";

		StringBuilder builder = new StringBuilder();
		for( GuideNode node : nodes ) {
			builder.append( node.getId() ).append( "," );
		}

		String ids = builder.toString();
		ids = ids.substring( 0, ids.length() - 1 );
		return ids;
	}

}
