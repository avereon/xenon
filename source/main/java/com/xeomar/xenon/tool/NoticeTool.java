package com.xeomar.xenon.tool;

import com.xeomar.xenon.ProductTool;
import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.Workpane;

public class NoticeTool extends ProductTool {

	public NoticeTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-notice" );
		setTitle( product.getResourceBundle().getString( "tool", "notice-name" ) );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

}
