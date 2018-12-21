package com.xeomar.xenon.tool.notice;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramProduct;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.Workpane;
import javafx.scene.layout.VBox;

/**
 * The notice tool displays the program notices that have been posted. The tool
 * allows the user to close/dismiss notices or click on the notice to execute
 * a program action.
 */
public class NoticeTool extends ProgramTool {

	// NEXT There will need to be a notice manager to work with
	private VBox noticeContainer;

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

	// TODO Unfinished

}
