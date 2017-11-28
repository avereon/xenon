package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.type.ProgramNoticeType;
import javafx.event.Event;

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
		getProgram().getResourceManager().open( getProgram().getResourceManager().createResource( ProgramNoticeType.uri ) );
	}

}
