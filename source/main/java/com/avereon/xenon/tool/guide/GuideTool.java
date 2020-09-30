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
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.lang.System.Logger;
import java.lang.ref.WeakReference;
import java.util.*;

public class GuideTool extends ProgramTool {

	private static final Logger log = Log.get();

	private final TreeView<GuideNode> guideTree;

	private final ToolActivatedWatcher toolActivatedWatcher;

	private final ToolConcealedWatcher toolConcealedWatcher;

	private final GuideTreeSelectedItemsListener selectedItemsListener;

	private final GuideSelectedItemsListener guideSelectedItemsListener;

	private GuideContext context;

	private ContextMenu contextMenu;

	private Guide guide;

	@SuppressWarnings( "WeakerAccess" )
	public GuideTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-guide" );
		guideTree = new TreeView<>();
		guideTree.setShowRoot( false );
		guideTree.setCellFactory( new GuideCellFactory() );
		getChildren().add( guideTree );

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
	 * Called when the selected items change in the guide and the TreeView
	 * needs to be updated. Per the TreeView documentation the last item in
	 * the list becomes the "single" selected item.
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

	private class ToolActivatedWatcher implements javafx.event.EventHandler<ToolEvent> {

		@Override
		public void handle( ToolEvent event ) {
			Tool tool = event.getTool();
			if( tool instanceof GuideTool ) return;
			if( tool instanceof GuidedTool ) {
				log.log( Log.DEBUG, "show guide: " + event.getTool().getClass().getName() );
				setGuideContext( ((GuidedTool)tool).getGuideContext() );
			} else {
				setGuide( null );
			}
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
		public void changed(
			ObservableValue<? extends Set<TreeItem<GuideNode>>> observable, Set<TreeItem<GuideNode>> oldValue, Set<TreeItem<GuideNode>> newValue
		) {
			// Disable the guide view selection change listener
			guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );

			setSelectedItems( newValue );

			// Re-enable the guide view selection change listener
			guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener );
		}

	}

	private class GuideCellFactory implements Callback<TreeView<GuideNode>, TreeCell<GuideNode>> {

		@Override
		public TreeCell<GuideNode> call( TreeView<GuideNode> param ) {
			return new GuideTreeCell();
		}

	}

	private class GuideTreeCell extends TreeCell<GuideNode> {

		private WeakReference<TreeItem<GuideNode>> treeItemRef;

		private InvalidationListener treeItemGraphicListener = observable -> updateDisplay( getItem(), isEmpty() );

		private WeakInvalidationListener weakTreeItemGraphicListener = new WeakInvalidationListener( treeItemGraphicListener );

		private InvalidationListener treeItemListener = observable -> doInvalidated();

		private WeakInvalidationListener weakTreeItemListener = new WeakInvalidationListener( treeItemListener );

		GuideTreeCell() {
			setDisclosureNode( null );
			treeItemProperty().addListener( weakTreeItemListener );
			if( getTreeItem() != null ) getTreeItem().graphicProperty().addListener( weakTreeItemGraphicListener );
		}

		@Override
		public void updateItem( GuideNode item, boolean empty ) {
			super.updateItem( item, empty );
			updateDisplay( item, empty );
		}

		private void updateDisplay( GuideNode item, boolean empty ) {
			setGraphic( empty ? null : getProgram().getIconLibrary().getIcon( item.getIcon() ) );
			setText( empty ? null : item.getName() );

			// FIXME This got rid of the triangle, but not the space it takes
			setDisclosureNode( null );
		}

		private void doInvalidated() {
			TreeItem<GuideNode> oldTreeItem = treeItemRef == null ? null : treeItemRef.get();
			if( oldTreeItem != null ) oldTreeItem.graphicProperty().removeListener( weakTreeItemGraphicListener );

			TreeItem<GuideNode> newTreeItem = getTreeItem();
			if( newTreeItem != null ) {
				newTreeItem.graphicProperty().addListener( weakTreeItemGraphicListener );
				treeItemRef = new WeakReference<>( newTreeItem );
			}
		}

	}

}
