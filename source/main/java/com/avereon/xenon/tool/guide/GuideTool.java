package com.avereon.xenon.tool.guide;

import com.avereon.product.Rb;
import com.avereon.settings.Settings;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import com.avereon.zarra.javafx.FxUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Callback;
import lombok.CustomLog;

import java.util.*;
import java.util.stream.Collectors;

@CustomLog
public class GuideTool extends ProgramTool {

	private static final DataFormat DATA_FORMAT = new DataFormat( "application/x-cartesia-layer" );

	private static final String DROP_ABOVE_HINT_STYLE = "-fx-border-color: -fx-focus-color; -fx-border-width: 0.125em 0 0 0; -fx-padding: 0.125em 0.25em 0.25em 0.25em;";

	private static final String DROP_CHILD_HINT_STYLE = "-fx-background-color: -ex-workspace-drop-hint;";

	private static final String DROP_BELOW_HINT_STYLE = "-fx-border-color: -fx-focus-color; -fx-border-width: 0 0 0.125em 0; -fx-padding: 0.25em 0.25em 0.125em 0.25em;";

	private final TreeView<GuideNode> guideTree;

	private final ToolActivatedWatcher toolActivatedWatcher;

	private final ToolConcealedWatcher toolConcealedWatcher;

	private final GuideToTreeExpandedItemsListener guideToTreeExpandedItemsListener;

	private final GuideToTreeSelectedItemsListener guideToTreeSelectedItemsListener;

	private final TreeToGuideSelectedItemsListener treeToGuideSelectedItemsListener;

	private final EventHandler<TreeItem.TreeModificationEvent<Object>> treeToGuideExpandedItemsListener;

	private GuideContext context;

	private ContextMenu contextMenu;

	private TreeCell<GuideNode> draggedCell;

	@SuppressWarnings( "WeakerAccess" )
	public GuideTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-guide" );
		guideTree = new TreeView<>();
		guideTree.setShowRoot( false );
		guideTree.setCellFactory( new GuideCellFactory() );
		ScrollPane scroller = new ScrollPane( guideTree );
		scroller.setFitToWidth( true );
		scroller.setFitToHeight( true );
		getChildren().add( scroller );

		toolActivatedWatcher = new ToolActivatedWatcher();
		toolConcealedWatcher = new ToolConcealedWatcher();
		guideToTreeExpandedItemsListener = new GuideToTreeExpandedItemsListener();
		guideToTreeSelectedItemsListener = new GuideToTreeSelectedItemsListener();
		treeToGuideSelectedItemsListener = new TreeToGuideSelectedItemsListener();
		treeToGuideExpandedItemsListener = e -> updateExpandedItems();
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

	@Override
	public boolean changeCurrentAsset() {
		return false;
	}

	@Override
	public ContextMenu getContextMenu() {
		return contextMenu == null ? super.getContextMenu() : contextMenu;
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( "tool", "guide-name" ) );
		setGraphic( getProduct().getProgram().getIconLibrary().getIcon( "guide" ) );
	}

	@Override
	protected void allocate() {
		// Listen for tool changes
		getWorkpane().addEventHandler( ToolEvent.ACTIVATED, toolActivatedWatcher );
		getWorkpane().addEventHandler( ToolEvent.CONCEALED, toolConcealedWatcher );
	}

	@Override
	protected void deallocate() {
		// Stop listening for tool changes
		getWorkpane().removeEventHandler( ToolEvent.CONCEALED, toolConcealedWatcher );
		getWorkpane().removeEventHandler( ToolEvent.ACTIVATED, toolActivatedWatcher );
	}

	private GuideContext getGuideContext() {
		return context;
	}

	private void setGuideContext( GuideContext context ) {
		this.context = context;
		doSetGuide( context.getCurrentGuide() );
		if( shouldEnableContextMenu( context ) ) {
			setContextGraphic( getProgram().getIconLibrary().getIcon( "context" ) );
			contextMenu = generateContextMenu( context );
		} else {
			setContextGraphic( null );
			contextMenu = null;
		}
	}

	private boolean shouldEnableContextMenu( GuideContext context ) {
		return context.getGuides().size() > 1;
	}

	private ContextMenu generateContextMenu( GuideContext context ) {
		ContextMenu contextMenu = new ContextMenu();
		context.getGuides().forEach( g -> contextMenu.getItems().add( generateContextMenuItem( g ) ) );
		return contextMenu;
	}

	private MenuItem generateContextMenuItem( Guide guide ) {
		String name = guide.getTitle();
		String icon = guide.getIcon();

		MenuItem item = new MenuItem( name, getProgram().getIconLibrary().getIcon( icon ) );
		item.setOnAction( e -> doSetGuide( guide ) );
		guide.titleProperty().addListener( ( p, o, n ) -> item.setText( n ) );
		guide.iconProperty().addListener( ( p, o, n ) -> item.setGraphic( getProgram().getIconLibrary().getIcon( n ) ) );

		return item;
	}

	private void doSetGuide( Guide newGuide ) {
		if( getGuideContext() == null ) return;

		Guide oldGuide = getGuideContext().getCurrentGuide();
		getGuideContext().dispatch( new GuideEvent( this, GuideEvent.GUIDE_CHANGING, oldGuide, newGuide ) );

		// Disconnect the old guide
		if( oldGuide != null ) {
			// Remove the tree to guide expansion listener
			TreeItem<?> root = guideTree.getRoot();
			if( root != null ) {
				root.removeEventHandler( TreeItem.branchExpandedEvent(), treeToGuideExpandedItemsListener );
				root.removeEventHandler( TreeItem.branchCollapsedEvent(), treeToGuideExpandedItemsListener );
			}

			// Remove the tree to guide selected items listener
			guideTree.getSelectionModel().getSelectedIndices().removeListener( treeToGuideSelectedItemsListener );

			// Remove the guide to tree selected item property listener
			getGuideContext().selectedItemsProperty().removeListener( guideToTreeSelectedItemsListener );

			// Remove the guide to tree expanded item property listener
			getGuideContext().expandedItemsProperty().removeListener( guideToTreeExpandedItemsListener );

			// Unset the guide view focused property
			getGuideContext().focusedProperty().unbind();

			// Unset the guide view selection mode
			guideTree.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );

			// Unset the guide view root
			guideTree.setRoot( null );
		}

		if( newGuide != null ) getGuideContext().setCurrentGuide( newGuide );

		// Connect the new guide
		if( newGuide != null ) {
			// Set the guide view root
			guideTree.setRoot( newGuide.getRoot() );

			// Set the tree view selection mode
			guideTree.getSelectionModel().setSelectionMode( newGuide.getSelectionMode() );

			// Set the expanded items
			getGuideContext().getExpandedItems().forEach( i -> i.setExpanded( true ) );

			// Set the selected items
			Set<TreeItem<GuideNode>> items = getGuideContext().selectedItemsProperty().get();
			if( items == null ) {
				guideTree.getSelectionModel().selectFirst();
			} else {
				doSetSelectedItems( items );
			}

			String title = newGuide.getTitle();
			if( TextUtil.isEmpty( title ) ) title = Rb.text( "tool", "guide-name" );
			setTitle( title );

			String icon = newGuide.getIcon();
			if( TextUtil.isEmpty( icon ) ) icon = "guide";
			setGraphic( getProduct().getProgram().getIconLibrary().getIcon( icon ) );

			// Bind the focused property
			getGuideContext().focusedProperty().bind( guideTree.focusedProperty() );

			// Add the guide to tree expanded item property listener
			getGuideContext().expandedItemsProperty().addListener( guideToTreeExpandedItemsListener );

			// Add the guide to tree selected item property listener
			getGuideContext().selectedItemsProperty().addListener( guideToTreeSelectedItemsListener );

			// Add the tree to guide selected items listener
			guideTree.getSelectionModel().getSelectedIndices().addListener( treeToGuideSelectedItemsListener );

			// Set the tree to guide expansion listener
			TreeItem<?> root = guideTree.getRoot();
			if( root != null ) {
				root.addEventHandler( TreeItem.branchExpandedEvent(), treeToGuideExpandedItemsListener );
				root.addEventHandler( TreeItem.branchCollapsedEvent(), treeToGuideExpandedItemsListener );
			}
		}

		getGuideContext().dispatch( new GuideEvent( this, GuideEvent.GUIDE_CHANGED, oldGuide, newGuide ) );
	}

	private void updateExpandedItems() {
		getGuideContext().setExpandedItems( FxUtil.flatTree( guideTree.getRoot() ).stream().filter( (TreeItem::isExpanded) ).filter( ( item ) -> !item.isLeaf() ).collect( Collectors.toSet() ) );
	}

	private void doSetExpandedItems( Set<? extends TreeItem<GuideNode>> expandedItems ) {
		expandedItems.forEach( i -> Fx.run( () -> i.setExpanded( true ) ) );
	}

	/**
	 * Called when the selected items change in the guide and the TreeView needs
	 * to be updated. Per the TreeView documentation the last item in the list
	 * becomes the "single" selected item.
	 *
	 * @param selectedItems The selected items list.
	 */
	private void doSetSelectedItems( Set<? extends TreeItem<GuideNode>> selectedItems ) {
		// NOTE The tree should already be expanded before calling this method

		// Map the guide view tree item ids to indexes
		int index = 0;
		TreeItem<GuideNode> item;
		Map<String, Integer> indexMap = new HashMap<>();
		while( (item = guideTree.getTreeItem( index )) != null ) {
			indexMap.put( item.getValue().getId(), index++ );
		}

		// Determine the selected node indexes
		List<Integer> indexList = new ArrayList<>( selectedItems.size() );
		for( TreeItem<GuideNode> selectedItem : selectedItems ) {
			Integer itemIndex = indexMap.get( selectedItem.getValue().getId() );
			if( itemIndex != null ) indexList.add( itemIndex );
		}

		// If there are no selected items just return
		if( indexList.size() == 0 ) return;

		// Set the selected indexes
		int[] indexes = indexList.stream().mapToInt( value -> value ).toArray();
		guideTree.getSelectionModel().selectIndices( indexes[ 0 ], indexes );
	}

	private void expandAndCollapsePaths( TreeItem<GuideNode> selectedItem ) {
		Fx.run( () -> {
			Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
			boolean collapse = settings.get( "workspace-guide-auto-collapse", Boolean.class, false );
			boolean expand = settings.get( "workspace-guide-auto-expand", Boolean.class, false );

			// Expand the selected item
			if( expand && selectedItem != null ) selectedItem.setExpanded( true );

			// Collapse items that are not parents of the selected item
			if( expand && collapse ) {
				int count = guideTree.getExpandedItemCount();
				List<TreeItem<GuideNode>> collapseItems = new ArrayList<>();

				for( int index = 0; index < count; index++ ) {
					TreeItem<GuideNode> item = guideTree.getTreeItem( index );
					if( !FxUtil.isParentOf( item, selectedItem ) ) collapseItems.add( item );
				}

				for( TreeItem<GuideNode> item : collapseItems ) {
					item.setExpanded( false );
				}
			}
		} );
	}

	@SuppressWarnings( "unchecked" )
	private void doDragDetected( MouseEvent event ) {
		if( !getGuideContext().isDragAndDropEnabled() ) return;

		TreeCell<GuideNode> target = (TreeCell<GuideNode>)event.getSource();
		draggedCell = target;

		// Root node cannot be moved
		if( target == null || target.getTreeItem().getParent() == null ) return;

		ClipboardContent content = new ClipboardContent();
		content.put( DATA_FORMAT, "" );
		Dragboard db = target.startDragAndDrop( TransferMode.MOVE );
		db.setContent( content );
		db.setDragView( target.snapshot( null, null ) );
		event.consume();
	}

	@SuppressWarnings( "unchecked" )
	private void doDragOver( DragEvent event ) {
		if( !event.getDragboard().hasContent( DATA_FORMAT ) ) return;

		TreeCell<GuideNode> target = (TreeCell<GuideNode>)event.getSource();
		TreeItem<GuideNode> thisItem = target.getTreeItem();
		TreeItem<GuideNode> draggedItem = draggedCell.getTreeItem();

		// can't drop on itself
		if( draggedItem == null || thisItem == null || thisItem == draggedItem ) return;

		// ignore if this is the root
		if( draggedItem.getParent() == null ) {
			doClearDropLocation( event );
			return;
		}

		event.acceptTransferModes( TransferMode.MOVE );

		Guide.Drop drop = determineDrop( target, event.getX(), event.getY() );
		switch( drop ) {
			case ABOVE -> target.setStyle( DROP_ABOVE_HINT_STYLE );
			case CHILD -> target.setStyle( DROP_CHILD_HINT_STYLE );
			case BELOW -> target.setStyle( DROP_BELOW_HINT_STYLE );
			case NONE -> doClearDropLocation( event );
		}
	}

	@SuppressWarnings( "unchecked" )
	private void doDrop( DragEvent event ) {
		if( !event.getDragboard().hasContent( DATA_FORMAT ) ) return;

		TreeCell<GuideNode> target = (TreeCell<GuideNode>)event.getSource();
		TreeItem<GuideNode> draggedItem = draggedCell.getTreeItem();
		getGuideContext().getCurrentGuide().moveNode( draggedItem.getValue(), target.getTreeItem().getValue(), determineDrop( target, event.getX(), event.getY() ) );

		event.setDropCompleted( true );
	}

	@SuppressWarnings( "unchecked" )
	private void doClearDropLocation( DragEvent event ) {
		TreeCell<GuideNode> target = (TreeCell<GuideNode>)event.getSource();
		target.setStyle( "" );
	}

	private Guide.Drop determineDrop( Node node, double x, double y ) {
		double h = node.getBoundsInLocal().getHeight();
		if( y < 0 ) return Guide.Drop.NONE;
		if( y < 0.25 * h ) return Guide.Drop.ABOVE;
		if( y < 0.75 * h ) return Guide.Drop.CHILD;
		if( y < h ) return Guide.Drop.BELOW;
		return Guide.Drop.NONE;
	}

	private class ToolActivatedWatcher implements javafx.event.EventHandler<ToolEvent> {

		@Override
		public void handle( ToolEvent event ) {
			// NOTE This logic has been reworked a couple of times to change the behavior.
			// The next time this logic is reworked here are some things to keep in mind:
			// - Only guided tools should show/hide guides
			// - When a guided tool is closed it should also close it's own guide
			// - There is an ongoing debate whether tools that don't have guides should hide existing guides
			// - Some tools should definitely not hide a guide: GuideTool and PropertiesTool

			Tool tool = event.getTool();
			if( tool instanceof GuidedTool ) setGuideContext( ((GuidedTool)tool).getGuideContext() );
		}

	}

	private class ToolConcealedWatcher implements javafx.event.EventHandler<ToolEvent> {

		@Override
		public void handle( ToolEvent event ) {
			Tool tool = event.getTool();
			if( !(tool instanceof GuidedTool) ) return;
			log.atFine().log( "hide guide: %s", event.getTool().getClass().getName() );
			doSetGuide( null );
		}
	}

	private class TreeToGuideSelectedItemsListener implements ListChangeListener<Integer> {

		@Override
		public void onChanged( Change<? extends Integer> change ) {
			List<Integer> changedIndexes = new ArrayList<>( change.getList() );
			Set<TreeItem<GuideNode>> items = new HashSet<>( changedIndexes.size() );
			for( int index : changedIndexes ) {
				items.add( guideTree.getTreeItem( index ) );
			}

			if( items.size() > 0 ) expandAndCollapsePaths( items.iterator().next() );

			getGuideContext().setSelectedItems( items );
		}

	}

	/**
	 * This listener is notified when the expanded nodes are changed in the guide context.
	 */
	private class GuideToTreeExpandedItemsListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed( ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue ) {
			TreeItem<?> root = guideTree.getRoot();

			// Disable the tree to guide expanded change listener
			if( root != null ) {
				root.removeEventHandler( TreeItem.branchExpandedEvent(), treeToGuideExpandedItemsListener );
				root.removeEventHandler( TreeItem.branchCollapsedEvent(), treeToGuideExpandedItemsListener );
			}

			doSetExpandedItems( newValue );

			// Re-enable the tree to guide expanded change listener
			if( root != null ) {
				root.addEventHandler( TreeItem.branchExpandedEvent(), treeToGuideExpandedItemsListener );
				root.addEventHandler( TreeItem.branchCollapsedEvent(), treeToGuideExpandedItemsListener );
			}
		}

	}

	/**
	 * This listener is notified when the selected nodes are changed in the guide context.
	 */
	private class GuideToTreeSelectedItemsListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed( ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue ) {
			// Disable the tree to guide selection change listener
			guideTree.getSelectionModel().getSelectedIndices().removeListener( treeToGuideSelectedItemsListener );

			doSetSelectedItems( newValue );

			// Re-enable the tree to guide selection change listener
			guideTree.getSelectionModel().getSelectedIndices().addListener( treeToGuideSelectedItemsListener );
		}

	}

	private class GuideCellFactory implements Callback<TreeView<GuideNode>, TreeCell<GuideNode>> {

		@Override
		public TreeCell<GuideNode> call( TreeView<GuideNode> treeView ) {
			TreeCell<GuideNode> cell = new GuideTreeCell();
			cell.setOnDragDetected( GuideTool.this::doDragDetected );
			cell.setOnDragOver( GuideTool.this::doDragOver );
			cell.setOnDragExited( GuideTool.this::doClearDropLocation );
			cell.setOnDragDropped( GuideTool.this::doDrop );
			cell.setOnDragDone( GuideTool.this::doClearDropLocation );
			return cell;
		}

	}

	private class GuideTreeCell extends TreeCell<GuideNode> {

		private final InvalidationListener iconListener;

		private TreeItem<GuideNode> treeItem;

		GuideTreeCell() {
			treeItemProperty().addListener( observable -> doInvalidated() );
			iconListener = o -> updateItem( getItem(), isEmpty() );
			doInvalidated();
		}

		@Override
		public void updateItem( GuideNode item, boolean empty ) {
			super.updateItem( item, empty );
			setGraphic( empty ? null : getProgram().getIconLibrary().getIcon( item.getIcon() ) );
			setText( empty ? null : item.toString() );
		}

		private void doInvalidated() {
			TreeItem<GuideNode> oldTreeItem = treeItem;
			TreeItem<GuideNode> newTreeItem = getTreeItem();
			if( newTreeItem == oldTreeItem ) return;

			if( oldTreeItem != null ) oldTreeItem.graphicProperty().removeListener( iconListener );
			treeItem = newTreeItem;
			if( newTreeItem != null ) newTreeItem.graphicProperty().addListener( iconListener );
		}

	}

}
