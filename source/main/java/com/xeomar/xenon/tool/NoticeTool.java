package com.xeomar.xenon.tool;

import com.xeomar.product.Product;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.workarea.Workpane;

public class NoticeTool extends AbstractTool {

	public NoticeTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-notice" );
		setGraphic( ((Program)product).getIconLibrary().getIcon( "notice" ) );
		setTitle( product.getResourceBundle().getString( "tool", "notice-name" ) );
	}

	@Override
	public Workpane.Placement getPlacement() {
		return Workpane.Placement.DOCK_RIGHT;
	}

}
