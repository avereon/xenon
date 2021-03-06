package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.type.ProgramTaskType;
import javafx.event.ActionEvent;

public class TaskAction extends ProgramAction {

	public TaskAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getAssetManager().openAsset( ProgramTaskType.URI );
	}

}
