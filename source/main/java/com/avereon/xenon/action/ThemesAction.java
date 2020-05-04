package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.type.ProgramThemesType;
import javafx.event.ActionEvent;

public class ThemesAction extends Action {

	public ThemesAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		getProgram().getAssetManager().openAsset( ProgramThemesType.URI );
	}

}
