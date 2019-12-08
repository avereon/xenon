package com.avereon.xenon.tool.guide;

import com.avereon.settings.Settings;
import com.avereon.util.LogUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.util.FxUtil;
import com.avereon.xenon.workpane.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class GuideTool extends ProgramTool {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Guide guide;

	private TreeView<GuideNode> guideTree;

	private ActiveToolWatcher activeToolWatcher;

	private GuideTreeSelectedItemsListener selectedItemsListener;

	private GuideSelectedItemsListener guideSelectedItemsListener;

	//private GuideActiveListener guideActiveListener;

	@SuppressWarnings( "WeakerAccess" )
	public GuideTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-guide" );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "guide" ) );
		setTitle( product.rb().text( "tool", "guide-name" ) );
		guideTree = new TreeView<>();
		guideTree.setShowRoot( false );
		getChildren().add( guideTree );

		activeToolWatcher = new ActiveToolWatcher();
		selectedItemsListener = new GuideTreeSelectedItemsListener();
		guideSelectedItemsListener = new GuideSelectedItemsListener();
		//guideActiveListener = new GuideActiveListener();
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

	@Override
	protected void allocate() {
		// Attach to the workpane and listen for current tool changes
		getWorkpane().addWorkpaneListener( activeToolWatcher );
	}

	@Override
	protected void deallocate() {
		// Disconnect from the asset guide
		getWorkpane().removeWorkpaneListener( activeToolWatcher );
	}

	private void setGuide( Guide guide ) {
		// Disconnect the old guide
		if( this.guide != null ) {
			// Remove the guide selected item property listener
			this.guide.selectedItemsProperty().removeListener( guideSelectedItemsListener );

			//			// Remove the guide active property listener
			//			this.guide.activeProperty().removeListener( guideActiveListener );

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

			//			// Add the guide active property listener
			//			this.guide.activeProperty().addListener( guideActiveListener );

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

	private class ActiveToolWatcher implements WorkpaneListener {

		@Override
		public void handle( WorkpaneEvent event ) {
			if( !(event instanceof WorkpaneToolEvent) ) return;

			WorkpaneToolEvent toolEvent = (WorkpaneToolEvent)event;
			Tool tool = toolEvent.getTool();
			if( !(tool instanceof GuidedTool) ) return;

			GuidedTool guidedTool = (GuidedTool)tool;
			switch( event.getType() ) {
				case TOOL_ACTIVATED: {
					log.debug( "show guide: " + ((WorkpaneToolEvent)event).getTool().getClass().getName() );
					setGuide( guidedTool.getGuide() );
					break;
				}
				case TOOL_CONCEALED: {
					log.debug( "hide guide: " + ((WorkpaneToolEvent)event).getTool().getClass().getName() );
					setGuide( null );
				}
			}
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

	//	private class GuideActiveListener implements ChangeListener<Boolean> {
	//
	//		@Override
	//		public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
	//			log.warn( "Guide active: " + newValue );
	//		}
	//
	//	}

}
