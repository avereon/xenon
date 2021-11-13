package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.asset.type.ProgramIndexSearchType;
import com.avereon.xenon.tool.IndexSearchTool;
import com.avereon.xenon.workpane.Tool;
import javafx.event.ActionEvent;

import java.util.Set;

public class IndexSearchAction extends ProgramAction {

	public IndexSearchAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpaneTools( IndexSearchTool.class );

		if( tools.isEmpty() ) {
			// Open the index search tool
			getProgram().getAssetManager().openAsset( ProgramIndexSearchType.URI );
		} else {
			// Close the index search tool
			tools.forEach( Tool::close );
		}
	}

}

