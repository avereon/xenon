package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.Settings;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class GuideTool extends ProgramTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private TreeView<GuideNode> guideTree;

	private ActiveGuideListener activeGuideListener;

	private GuideTreeSelectedItemsListener selectedItemsListener;

	private GuideSelectedItemsListener guideSelectedItemsListener;

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

		log.warn( "switch guide: " + ((WorkpaneToolEvent)event).getTool().getTitle() );

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

		// Add the tree selected items listener
		if( selectedItemsListener != null ) guideTree.getSelectionModel().getSelectedIndices().removeListener( selectedItemsListener );
		guideTree.getSelectionModel().getSelectedIndices().addListener( selectedItemsListener = new GuideTreeSelectedItemsListener( guide ) );

		// Add the guide active property listener
		if( activeGuideListener != null ) guide.activeProperty().removeListener( activeGuideListener );
		guide.activeProperty().addListener( activeGuideListener = new ActiveGuideListener( guide ) );

		// Add the guide selected item property listener
		// This listens to the guide for changes to the selected items
		if( guideSelectedItemsListener != null ) guide.selectedItemsProperty().removeListener( guideSelectedItemsListener );
		guide.selectedItemsProperty().addListener( guideSelectedItemsListener = new GuideSelectedItemsListener() );

		// Set the selected items
		Set<TreeItem<GuideNode>> items = guide.selectedItemsProperty().get();
		if( items == null ) {
			guideTree.getSelectionModel().selectFirst();
		} else {
			setSelectedItems( items );
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
		Map<String, Integer> indexMap = new HashMap<>( );
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

	private class GuideTreeSelectedItemsListener implements ListChangeListener<Integer> {

		private Guide guide;

		public GuideTreeSelectedItemsListener( Guide guide ) {
			this.guide = guide;
		}

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

}
