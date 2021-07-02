package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.type.PropertiesType;
import javafx.event.ActionEvent;

public class PropertiesAction extends ProgramAction {

	public PropertiesAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getAssetManager().openAsset( PropertiesType.URI );
	}

}
