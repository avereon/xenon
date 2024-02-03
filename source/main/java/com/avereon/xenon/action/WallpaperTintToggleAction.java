package com.avereon.xenon.action;

import com.avereon.settings.Settings;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.ProgramSettings;
import javafx.event.ActionEvent;

public class WallpaperTintToggleAction extends ProgramAction {

	public WallpaperTintToggleAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Settings settings = getProgram().getSettingsManager().getSettings( ProgramSettings.PROGRAM );
		boolean enabled = Boolean.parseBoolean( settings.get( "workspace-scenery-tint-enabled", "true" ) );
		settings.set( "workspace-scenery-tint-enabled", !enabled );
	}

}
