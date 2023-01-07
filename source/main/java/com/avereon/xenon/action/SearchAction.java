package com.avereon.xenon.action;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.asset.type.ProgramSearchType;
import com.avereon.xenon.tool.SearchTool;
import com.avereon.xenon.workpane.Tool;
import javafx.event.ActionEvent;

import java.util.Set;

public class SearchAction extends ProgramAction {

	public SearchAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpaneTools( SearchTool.class );

		if( tools.isEmpty() ) {
			// Open the index search tool
			getProgram().getAssetManager().openAsset( ProgramSearchType.URI );
		} else {
			// Close the index search tool
			tools.forEach( Tool::close );
		}
	}

}

