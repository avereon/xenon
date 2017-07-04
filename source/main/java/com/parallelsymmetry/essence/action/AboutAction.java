package com.parallelsymmetry.essence.action;

import com.parallelsymmetry.essence.Action;
import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.workarea.Workpane;
import com.parallelsymmetry.essence.workspace.ToolInstanceMode;
import com.parallelsymmetry.essence.worktool.Tool;
import javafx.event.Event;

import java.net.URI;

public class AboutAction extends Action {

	private Resource resource;

	public AboutAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( Event event ) {
		if( resource == null ) {
			try {
				resource = program.getResourceManager().createResource( URI.create( "program:about" ) );
				program.getResourceManager().openResourcesAndWait( resource );
				resource.setModel( program.getMetadata() );
			} catch( Exception exception ) {
				log.warn( "Error opening about resource", exception );
			}
		}

		ProductTool tool = program.getToolManager().getTool( resource );
		System.out.println( "Tool instance mode: " + tool.getInstanceMode() );

		// FIXME Make a general method to handle SINGLETON tools
		Workpane workpane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		if( tool.getInstanceMode() == ToolInstanceMode.SINGLETON ) {
			Tool existingTool = null;
			for( Tool paneTool : workpane.getTools() ) {
				if( paneTool.getClass() == tool.getClass() ) {
					existingTool = paneTool;
					break;
				}
			}
			if( existingTool == null ) {
				workpane.addTool( tool, true );
			} else {
				workpane.setActiveTool( existingTool );
			}
		} else {
			workpane.addTool( tool, true );
		}
	}

}
