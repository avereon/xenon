package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.worktool.ToolException;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class GuideTool extends ProductTool {

	private TreeView guideView;

	public GuideTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-guide" );
		setTitle( product.getResourceBundle().getString( "tool", "guide-name" ) );
		getChildren().add( guideView = new TreeView() );
		guideView.setShowRoot( false );
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
	}

	@Override
	protected void resourceRefreshed() {
		// Update the guide? Or will the guide be updated by the resource?
	}

	@Override
	protected void deallocate() throws ToolException {
		// Disconnect from the resource guide
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

}
