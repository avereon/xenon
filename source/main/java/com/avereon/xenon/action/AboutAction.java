package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.resource.type.ProgramAboutType;
import javafx.event.ActionEvent;

public class AboutAction extends ProgramAction {

	public AboutAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getResourceManager().openAsset( ProgramAboutType.URI );
	}

}
