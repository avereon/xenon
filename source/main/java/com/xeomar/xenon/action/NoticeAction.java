package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramNoticeType;
import com.xeomar.xenon.tool.notice.NoticeTool;
import com.xeomar.xenon.workarea.Tool;
import javafx.event.Event;

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
	public void handle( Event event ) {
		Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpane().getTools( NoticeTool.class );

		if( tools.size() > 0 ) {
			// Close the notice tools
			tools.forEach( Tool::close );
		} else {
			// Open the notice tool
			getProgram().getNoticeManager().readAll();
			getProgram().getResourceManager().open( ProgramNoticeType.URI );
		}
	}

}
