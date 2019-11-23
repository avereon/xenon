package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramAboutType;
import com.avereon.xenon.tool.about.AboutTool;
import javafx.event.ActionEvent;

import java.net.URI;

public class AboutAction extends Action {

	public AboutAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		URI uri = URI.create( ProgramAboutType.URI + "#" + AboutTool.SUMMARY );
		getProgram().getResourceManager().open( uri );
	}

}
