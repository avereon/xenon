package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.workarea.WorkpaneEvent;
import com.parallelsymmetry.essence.worktool.ToolException;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class GuideTool extends ProductTool {

	private TreeView guideView;

	public GuideTool( Product product, Resource resource ) {
		// FIXME Maybe the resource should be program:guide
		// and it should listen for the current resource changes in the workarea
		super( product, resource );
		setId( "tool-guide" );
		setTitle( product.getResourceBundle().getString( "tool", "guide-name" ) );
		getChildren().add( guideView = new TreeView() );
		guideView.setShowRoot( false );
	}

	@SuppressWarnings( "unchecked" )
	private void switchGuide( WorkpaneEvent event ) {
		if( event.getType() != WorkpaneEvent.Type.TOOL_ACTIVATED ) return;

		Guide guide = event.getTool().getResource().getResource( Guide.GUIDE_KEY );
		if( guide == null ) return;

		guideView.setRoot( guide.getRoot() );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	protected void resourceReady() throws ToolException {
		// Connect to the resource guide
		Guide guide = getResource().getResource( Guide.GUIDE_KEY );
		if( guide == null ) return;

		// Set guide tree root
		TreeItem root = guide.getRoot();
		if( root != null ) guideView.setRoot( root );

		// Set guide selection mode
		guideView.getSelectionModel().setSelectionMode( guide.getSelectionMode() );

		guideView.getSelectionModel().selectedItemProperty().addListener( ( obs, oldSelection, newSelection ) -> {
			guide.setSelectedItem( (TreeItem)newSelection );
		} );

		guide.activeProperty().addListener( ( observable, oldValue, newValue ) -> {
			if( !newValue ) guideView.setRoot( null );
		} );
	}

	@Override
	protected void resourceRefreshed() {
		// Update the guide? Or will the guide be updated by the resource?
	}

	@Override
	protected void allocate() throws ToolException {
		// Attach to the workpane and listen for current tool changes
		getWorkpane().addWorkpaneListener( this::switchGuide );
	}

	@Override
	protected void deallocate() throws ToolException {
		// Disconnect from the resource guide
	}

}
