package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.type.ProgramNoticeType;
import com.avereon.xenon.tool.NoticeTool;
import com.avereon.xenon.workpane.Tool;
import javafx.event.ActionEvent;

import java.util.Set;

public class NoticeAction extends Action {

	public NoticeAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpane().getTools( NoticeTool.class );

		if( tools.size() > 0 ) {
			// Close the notice tools
			tools.forEach( Tool::close );
		} else {
			// Open the notice tool
			getProgram().getNoticeManager().readAll();
			getProgram().getAssetManager().openAsset( ProgramNoticeType.URI );
		}
	}

}
