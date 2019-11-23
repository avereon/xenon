package com.avereon.xenon.action;

import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.type.ProgramSettingsType;
import com.avereon.xenon.tool.settings.SettingsTool;
import javafx.event.ActionEvent;

import java.net.URI;

public class SettingsAction extends Action {

	public SettingsAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		URI uri = URI.create( ProgramSettingsType.URI + "#" + SettingsTool.GENERAL );
		getProgram().getResourceManager().open( uri );
	}

}
