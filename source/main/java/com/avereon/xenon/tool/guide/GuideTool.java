package com.avereon.xenon.tool.guide;

import com.avereon.settings.Settings;
import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.Tool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.FxUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.lang.System.Logger;
import java.util.*;

public class GuideTool extends ProgramTool {

	private static final Logger log = Log.get();

	private static final DataFormat DATA_FORMAT = new DataFormat( "application/x-cartesia-layer" );

	private static final String DROP_ABOVE_HINT_STYLE = "-fx-border-color: -fx-focus-color; -fx-border-width: 0.125em 0 0 0; -fx-padding: 0.125em 0.25em 0.25em 0.25em;";

	private static final String DROP_CHILD_HINT_STYLE = "-fx-background-color: -ex-workspace-drop-hint;";

	private static final String DROP_BELOW_HINT_STYLE = "-fx-border-color: -fx-focus-color; -fx-border-width: 0 0 0.125em 0; -fx-padding: 0.25em 0.25em 0.125em 0.25em;";

	private final TreeView<GuideNode> guideTree;

	private final ToolActivatedWatcher toolActivatedWatcher;

	private final ToolConcealedWatcher toolConcealedWatcher;

	private final GuideTreeSelectedItemsListener selectedItemsListener;

	private final GuideSelectedItemsListener guideSelectedItemsListener;

	private GuideContext context;

	private ContextMenu contextMenu;

	private Guide guide;

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
		getChildren().add( scroller );

		toolActivatedWatcher = new ToolActivatedWatcher();
		toolConcealedWatcher = new ToolConcealedWatcher();
		selectedItemsListener = new GuideTreeSelectedItemsListener();
		guideSelectedItemsListener = new GuideSelectedItemsListener();
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
		setTitle( getProduct().rb().text( "tool", "guide-name" ) );
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

	private void setGuideContext( GuideContext context ) {
		this.context = context;
		setGuide( context.getCurrentGuide() );
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
		item.setOnAction( e -> setGuide( guide ) );
		guide.titleProperty().addListener( ( p, o, n ) -> item.setText( n ) );
		guide.iconProperty().addListener( ( p, o, n ) -> item.setGraphic( getProgram().getIconLibrary().getIcon( n ) ) );

		return item;
	}

	private void setGuide( Guide guide ) {
		Guide oldGuide = this.guide;
		if( context != null ) context.dispatch( new GuideEvent( this, GuideEvent.GUIDE_CHANGING, oldGuide, guide ) );

		// Disconnect the old guide
		if( this.guide != null ) {
			// Remove the guide selected item property listener
			this.guide.selectedItemsProperty().removeListener( guideSelectedItemsListener );

			// Remove the tree selected items listener
			guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );

			// Unset the guide view selection mode
			guideTree.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );

			// Unset the guide view root
			guideTree.setRoot( null );
		}

		this.guide = guide;

		// Connect the new guide
		if( this.guide != null ) {
			// Set the guide view root
			guideTree.setRoot( this.guide.getRoot() );

			// Set the guide view selection mode
			guideTree.getSelectionModel().setSelectionMode( this.guide.getSelectionMode() );

			// Add the tree selected items listener
			guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener );

			// Add the guide selected item property listener
			// This listens to the guide for changes to the selected items
			this.guide.selectedItemsProperty().addListener( guideSelectedItemsListener );

			// Set the selected items
			Set<TreeItem<GuideNode>> items = this.guide.selectedItemsProperty().get();
			if( items == null ) {
				guideTree.getSelectionModel().selectFirst();
			} else {
				setSelectedItems( items );
			}

			String title = guide.getTitle();
			if( TextUtil.isEmpty( title ) ) title = getProduct().rb().text( "tool", "guide-name" );
			setTitle( title );

			String icon = guide.getIcon();
			if( TextUtil.isEmpty( icon ) ) icon = "guide";
			setGraphic( getProduct().getProgram().getIconLibrary().getIcon( icon ) );
		}

		if( context != null ) context.dispatch( new GuideEvent( this, GuideEvent.GUIDE_CHANGED, oldGuide, guide ) );
	}

	/**
	 * Called when the selected items change in the guide and the TreeView needs to be updated. Per the TreeView documentation the last item in the list becomes the "single" selected item.
	 *
	 * @param selectedItems The selected items list.
	 */
	private void setSelectedItems( Set<? extends TreeItem<GuideNode>> selectedItems ) {
		//		// The tree should already be expanded before calling this method
		//		for( TreeItem<GuideNode> item : selectedItems ) {
		//			item = item.getParent();
		//			while( item != null ) {
		//				item.setExpanded( true );
		//				item = item.getParent();
		//			}
		//		}

		// FIXME The following logic leaves selected item artifacts when auto-expand is on

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
	}

	@SuppressWarnings( "unchecked" )
	private void doDragDetected( MouseEvent event ) {
		if( !guide.isDragAndDropEnabled() ) return;

		TreeCell<GuideNode> target = (TreeCell<GuideNode>)event.getSource();
		draggedCell = target;

		// Root node cannot be moved
		if( target.getTreeItem().getParent() == null ) return;

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
		guide.moveNode( draggedItem.getValue(), target.getTreeItem().getValue(), determineDrop( target, event.getX(), event.getY() ) );

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
			// NOTE This logic has been reworked a couple of times to change the
			// behavior. The next time this logic is reworked here are some things
			// to keep in mind:
			// - Only guided tools should show/hide guides
			// - When a guided tool is closed it should also close it's own guide
			// - There is an ongoing debate whether tools that don't have guides should hide existing guides
			// - Some tools should definitely not hide a guide: GuideTool and PropertiesTool

			Tool tool = event.getTool();

			// Some tools should not cause the guide to change
			//if( tool instanceof GuideTool ) return;

			if( tool instanceof GuidedTool ) setGuideContext( ((GuidedTool)tool).getGuideContext() );
		}

	}

	private class ToolConcealedWatcher implements javafx.event.EventHandler<ToolEvent> {

		@Override
		public void handle( ToolEvent event ) {
			Tool tool = event.getTool();
			if( !(tool instanceof GuidedTool) ) return;
			log.log( Log.DEBUG, "hide guide: " + event.getTool().getClass().getName() );
			setGuide( null );
		}
	}

	private class GuideTreeSelectedItemsListener implements ListChangeListener<Integer> {

		@Override
		public void onChanged( Change<? extends Integer> change ) {
			List<Integer> changedIndexes = new ArrayList<>( change.getList() );
			Set<TreeItem<GuideNode>> items = new HashSet<>( changedIndexes.size() );
			for( int index : changedIndexes ) {
				items.add( guideTree.getTreeItem( index ) );
			}

			if( items.size() > 0 ) expandAndCollapsePaths( items.iterator().next() );

			guide.setSelectedItems( items );
		}

	}

	private class GuideSelectedItemsListener implements ChangeListener<Set<TreeItem<GuideNode>>> {

		@Override
		public void changed( ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue ) {
			// Disable the guide view selection change listener
			guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );

			setSelectedItems( newValue );

			// Re-enable the guide view selection change listener
			guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener );
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
			iconListener = o -> doUpdateItem( getItem(), isEmpty() );
			doInvalidated();
		}

		@Override
		public void updateItem( GuideNode item, boolean empty ) {
			super.updateItem( item, empty );
			doUpdateItem( item, empty );
		}

		private void doUpdateItem( GuideNode item, boolean empty ) {
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
