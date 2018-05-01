package com.xeomar.xenon.action;

import com.xeomar.xenon.Action;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.ResourceException;
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
		try {
			getProgram().getResourceManager().open( ProgramNoticeType.uri );
		} catch( ResourceException exception ) {
			log.error( "Error opening notice tool", exception );
		}
	}

}
