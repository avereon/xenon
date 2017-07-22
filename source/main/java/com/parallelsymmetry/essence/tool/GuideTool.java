package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.workarea.Workpane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class GuideTool extends ProductTool {

	private TreeView guide;

	private TreeItem root;

	public GuideTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-guide" );

		setTitle( product.getResourceBundle().getString( "tool", "guide-name" ) );

		guide = new TreeView(  );
		getChildren().add( guide );
	}

	@Override
	protected void resourceRefreshed() {
		// When the resource is refreshed
		// Had the resource guide been modified?

	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_LEFT;
	}

}
