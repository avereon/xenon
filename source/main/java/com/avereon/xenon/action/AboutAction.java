package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.type.ProgramAboutType;
import javafx.event.ActionEvent;

public class AboutAction extends ProgramAction {

	public AboutAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getAssetManager().openAsset( ProgramAboutType.URI );
	}

}
