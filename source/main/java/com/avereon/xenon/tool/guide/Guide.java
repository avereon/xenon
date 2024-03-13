package com.avereon.xenon.tool.guide;

import com.avereon.zarra.javafx.FxUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import lombok.CustomLog;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public class Guide {

	public enum Drop {
		ABOVE,
		CHILD,
		BELOW,
		NONE
	}

	public static final Guide EMPTY = new Guide();

	private static final Comparator<TreeItem<GuideNode>> guideNodeComparator = new GuideNodeTreeItemComparator();

	private final TreeItem<GuideNode> root;

	private SelectionMode selectionMode;

	private final StringProperty titleProperty;

	private final StringProperty iconProperty;

	// Passthrough from guide
	private final BooleanProperty dragAndDropEnabledProperty;

	public Guide() {
		this.root = new TreeItem<>();
		titleProperty = new SimpleStringProperty();
		iconProperty = new SimpleStringProperty();
		dragAndDropEnabledProperty = new SimpleBooleanProperty( false );
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
		// NOTE Intentionally do not force this code to run on the FX thread
		TreeItem<GuideNode> item = parent == null ? root : parent.getTreeItem();
		item.getChildren().add( node.getTreeItem() );
		item.getChildren().sort( guideNodeComparator );
		return node;
	}

	public final GuideNode removeNode( GuideNode node ) {
		node.getTreeItem().getParent().getChildren().remove( node.getTreeItem() );
		return node;
	}

	public final Guide clear() {
		return clear( null );
	}

	public final Guide clear( GuideNode node ) {
		TreeItem<GuideNode> item = node == null ? root : node.getTreeItem();
		item.getChildren().clear();
		return this;
	}

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode( SelectionMode selectionMode ) {
		this.selectionMode = selectionMode == null ? SelectionMode.SINGLE : selectionMode;
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

	/**
	 * This method can be overridden to capture when the nodes are moved in the guide.
	 *
	 * @param item The guide node that was moved
	 * @param target The guide node that the item was dropped on
	 * @param drop The guide drop hint
	 */
	protected void moveNode( GuideNode item, GuideNode target, Guide.Drop drop ) {}

	/* Only intended to be used by the GuideTool */
	public final TreeItem<GuideNode> getRoot() {
		return root;
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
		return FxUtil.flatTree( node ).stream().filter( n -> n.getValue().getId().equals( id ) ).findFirst().orElse( null );
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

	private static class GuideNodeTreeItemComparator implements Comparator<TreeItem<GuideNode>> {

		@Override
		public int compare( TreeItem<GuideNode> o1, TreeItem<GuideNode> o2 ) {
			GuideNode n1 = o1.getValue();
			GuideNode n2 = o2.getValue();
			return n1.getComparator().compare( n1, n2 );
			//return n2.getOrder() - n1.getOrder();
		}

	}

}
