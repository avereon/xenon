package com.avereon.xenon.action;

import com.avereon.settings.Settings;
import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramSettings;
import javafx.event.ActionEvent;

public class WallpaperToggleAction extends Action {

	public WallpaperToggleAction( Program program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		boolean enabled = Boolean.parseBoolean( settings.get( "workspace-scenery-image-enabled", "true" ) );
		settings.set( "workspace-scenery-image-enabled", !enabled );
	}

}
