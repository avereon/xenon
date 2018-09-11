package com.xeomar.xenon.tool.guide;

import com.xeomar.settings.Settings;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.util.FxUtil;
import com.xeomar.xenon.workarea.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.List;

public class GuideTool extends ProgramTool {

	private TreeView<GuideNode> guideTree;

	private GuideViewSelectedItemListener selectedItemListener;

	private GuideSelectedItemListener guideSelectedItemListener;

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
		// TODO guideTree.setSelectionModel( guide.getSelectionModel() );

		// Set the guide view selection mode
		guideTree.getSelectionModel().setSelectionMode( guide.getSelectionMode() );

		// Add the tree selected item listener
		if( selectedItemListener != null ) guideTree.getSelectionModel().selectedItemProperty().removeListener( selectedItemListener );
		guideTree.getSelectionModel().selectedItemProperty().addListener( selectedItemListener = new GuideViewSelectedItemListener( guide ) );

		// Add the guide active property listener
		if( activeGuideListener != null ) guide.activeProperty().removeListener( activeGuideListener );
		guide.activeProperty().addListener( activeGuideListener = new ActiveGuideListener( guide ) );

		// Add the guide selected item property listener
		if( guideSelectedItemListener != null ) guide.selectedItemProperty().removeListener( guideSelectedItemListener );
		guide.selectedItemProperty().addListener( guideSelectedItemListener = new GuideSelectedItemListener() );

		// Set the selected item
		TreeItem<GuideNode> item = guide.selectedItemProperty().get();
		System.out.println( "Guide pre-selected item: " + item );
		if( item == null ) {
			guideTree.getSelectionModel().selectIndices( 0 );
		} else {
			item.setExpanded( true );
		}
	}

	private void expandAndCollapsePaths( TreeItem<GuideNode> selectedItem ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		boolean collapse = settings.get( "workspace-guide-auto-collapse", Boolean.class, false );
		boolean expand = settings.get( "workspace-guide-auto-expand", Boolean.class, false );

		List<TreeItem<GuideNode>> collapseItems = new ArrayList<>();

		int count = guideTree.getExpandedItemCount();
		for( int index = 0; index < count; index++ ) {
			TreeItem<GuideNode> item = guideTree.getTreeItem( index );
			if( !FxUtil.isParentOf( item, selectedItem ) ) collapseItems.add( item );
		}

		if( expand && selectedItem != null ) selectedItem.setExpanded( true );

		if( expand && collapse ) {
			for( TreeItem<GuideNode> item : collapseItems ) {
				item.setExpanded( false );
			}
		}

	}

	private class GuideSelectedItemListener implements javafx.beans.value.ChangeListener<TreeItem<GuideNode>> {

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldSelection, TreeItem<GuideNode> newSelection ) {
			System.out.println( "Guide selected item: " + newSelection );
			guideTree.getSelectionModel().select( newSelection );
		}

	}

	private class GuideViewSelectedItemListener implements javafx.beans.value.ChangeListener<TreeItem<GuideNode>> {

		private Guide guide;

		GuideViewSelectedItemListener( Guide guide ) {
			this.guide = guide;
		}

		@Override
		public void changed( ObservableValue<? extends TreeItem<GuideNode>> observable, TreeItem<GuideNode> oldSelection, TreeItem<GuideNode> newSelection ) {
			guide.setSelectedItem( newSelection );
			expandAndCollapsePaths( newSelection );
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
