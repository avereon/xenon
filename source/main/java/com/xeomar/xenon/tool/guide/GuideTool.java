package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.*;

public class GuideTool extends ProgramTool {

	private TreeView<GuideNode> guideTree;

	private GuideViewSelectedItemListener selectedItemListener;

	private GuideViewSelectedItemsListener selectedItemsListener;

	private GuideSelectedItemListener guideSelectedItemListener;

	private GuideSelectedItemsListener guideSelectedItemsListener;

	private ActiveGuideListener activeGuideListener;

	public GuideTool( ProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-guide" );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "guide" ) );
		setTitle( product.getResourceBundle().getString( "tool", "guide-name" ) );
		guideTree = new TreeView<>();
		guideTree.setShowRoot( false );
		getChildren().add( guideTree );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	protected void resourceReady( ToolParameters parameters ) throws ToolException {
		// Connect to the resource guide
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		if( guide == null ) return;

		// Set guide tree root
		TreeItem root = guide.getRoot();
		if( root != null ) guideTree.setRoot( root );
	}

	@Override
	protected void resourceRefreshed() {
		// Update the guide? Or will the guide be updated by the resource?
	}

	@Override
	protected void allocate() throws ToolException {
		// Attach to the workpane and listen for current tool changes
		getWorkpane().addWorkpaneListener( this::switchGuide );

		Tool activeTool = getWorkpane().getActiveTool();
		if( activeTool != null ) setResourceGuide( activeTool.getResource() );
	}

	@Override
	protected void deallocate() throws ToolException {
		// Disconnect from the resource guide
	}

	private void switchGuide( WorkpaneEvent event ) {
		if( !(event instanceof WorkpaneToolEvent) ) return;

		WorkpaneToolEvent toolEvent = (WorkpaneToolEvent)event;
		switch( event.getType() ) {
			case TOOL_ACTIVATED: {
				setResourceGuide( toolEvent.getTool().getResource() );
				break;
			}
			case TOOL_CONCEALED: {
				setResourceGuide( null );
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	private void setResourceGuide( Resource resource ) {
		if( resource == null ) {
			guideTree.setRoot( null );
			return;
		}

		Guide guide = resource.getResource( Guide.GUIDE_KEY );
		if( guide == null ) return;

		// Set the guide view root
		guideTree.setRoot( guide.getRoot() );

		// Set the guide view selection mode
		guideTree.getSelectionModel().setSelectionMode( guide.getSelectionMode() );

		// Add the tree selected item listener
		if( selectedItemListener != null ) guideTree.getSelectionModel().selectedItemProperty().removeListener( selectedItemListener );
		guideTree.getSelectionModel().selectedItemProperty().addListener( selectedItemListener = new GuideViewSelectedItemListener( guide ) );

		// Add the tree selected items listener
		if( selectedItemsListener != null ) guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );
		guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener = new GuideViewSelectedItemsListener( guide ) );

		// TODO Add the tree expanded item listener

		// Add the guide active property listener
		if( activeGuideListener != null ) guide.activeProperty().removeListener( activeGuideListener );
		guide.activeProperty().addListener( activeGuideListener = new ActiveGuideListener( guide ) );

		// Add the guide selected item property listener
		// This listens to the guide for changes to the selected item
		if( guideSelectedItemListener != null ) guide.selectedItemProperty().removeListener( guideSelectedItemListener );
		guide.selectedItemProperty().addListener( guideSelectedItemListener = new GuideSelectedItemListener() );

		if( guideSelectedItemsListener != null ) guide.selectedItemsProperty().removeListener( guideSelectedItemsListener );
		guide.selectedItemsProperty().addListener( guideSelectedItemsListener = new GuideSelectedItemsListener() );

		// Set the selected item
		TreeItem<GuideNode> item = guide.selectedItemProperty().get();
		System.out.println( "Guide pre-selected item: " + item );
		if( item == null ) {
			guideTree.getSelectionModel().selectFirst();
		} else {
			item.setExpanded( true );
		}
	}

	/**
	 * Called when the selected items change in the guide and the TreeView
	 * needs to be updated. Per the TreeView documentation the last item in
	 * the list becomes the "single" selected item.
	 *
	 * @param selectedItems The selected items list.
	 */
	private void setSelectedItems( Set<? extends TreeItem<GuideNode>> selectedItems ) {
		// The tree should already be expanded before calling this method

		// Map the guide view tree item ids to indexes
		int count = guideTree.getExpandedItemCount();
		Map<String, Integer> indexMap = new HashMap<>( count );
		for( int index = 0; index < count; index++ ) {
			TreeItem<GuideNode> item = guideTree.getTreeItem( index );
			indexMap.put( item.getValue().getId(), index );
		}

		// Determine the selected node indexes
		List<Integer> indexList = new ArrayList<>( selectedItems.size() );
		for( TreeItem<GuideNode> item : selectedItems ) {
			Integer index = indexMap.get( item.getValue().getId() );
			if( index != null ) indexList.add( index );
		}

		// Clear the existing selection
		//guideTree.getSelectionModel().clearSelection();

		// If there are no selected items just return
		if( indexList.size() == 0 ) return;

		// Set the selected indexes
		int[] indexes = indexList.stream().mapToInt( i -> i ).toArray();
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

	private class GuideViewSelectedItemListener implements javafx.beans.value.ChangeListener<TreeItem<GuideNode>> {

		private Guide guide;

		public GuideViewSelectedItemListener( Guide guide ) {
			this.guide = guide;
		}

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldSelection, TreeItem<GuideNode> newSelection ) {
			guide.setSelectedItem( newSelection );
			//expandAndCollapsePaths( newSelection );
		}

	}

	private class GuideViewSelectedItemsListener implements ListChangeListener<Integer> {

		private Guide guide;

		public GuideViewSelectedItemsListener( Guide guide ) {
			this.guide = guide;
		}

		@Override
		public void onChanged( Change<? extends Integer> change ) {
			List<Integer> changedIndexes = new ArrayList<>( change.getList() );
			Set<TreeItem<GuideNode>> items = new HashSet<>( changedIndexes.size() );
			for( int index : changedIndexes ) {
				items.add( guideTree.getTreeItem( index ) );
			}

			guide.setSelectedItems( items );
			// TODO Auto expanding and collapsing paths with multi-select may not make sense
			//expandAndCollapsePaths( items );
		}

	}

	private class GuideSelectedItemListener implements javafx.beans.value.ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldSelection, TreeItem<GuideNode> newSelection ) {
			// Disable the guide view selection change listeners
			guideTree.getSelectionModel().selectedItemProperty().removeListener( selectedItemListener );
			guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );

			setSelectedItems( newSelection == null ? new HashSet<>() : Set.of( newSelection ) );

			// Re-enable the guide view selection change listeners
			guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener );
			guideTree.getSelectionModel().selectedItemProperty().addListener( selectedItemListener );
		}

	}

	private class GuideSelectedItemsListener implements SetChangeListener<TreeItem<GuideNode>> {

		@Override
		public void onChanged( Change<? extends TreeItem<GuideNode>> change ) {
			// Disable the guide view selection change listeners
			guideTree.getSelectionModel().selectedItemProperty().removeListener( selectedItemListener );
			guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );

			setSelectedItems( change.getSet() );

			// Re-enable the guide view selection change listeners
			guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener );
			guideTree.getSelectionModel().selectedItemProperty().addListener( selectedItemListener );
		}

	}

	private class ActiveGuideListener implements javafx.beans.value.ChangeListener<Boolean> {

		private Guide guide;

		ActiveGuideListener( Guide guide ) {
			this.guide = guide;
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public void changed( ObservableValue observable, Boolean oldSelection, Boolean newSelection ) {
			if( newSelection ) {
				guideTree.setRoot( guide.getRoot() );
			} else {
				guideTree.setRoot( null );
			}
		}

	}

}
