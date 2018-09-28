package com.xeomar.xenon.tool.basic;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.Workpane;

// FIXME Unfinished

public class NoticeTool extends ProgramTool {

	public NoticeTool( ProgramProduct product, Resource resource ) {
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
