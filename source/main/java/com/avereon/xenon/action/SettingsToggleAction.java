package com.avereon.xenon.action;

import com.avereon.xenon.ProgramAction;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.workpane.Tool;
import javafx.event.ActionEvent;

import java.util.Set;

public class SettingsToggleAction extends ProgramAction {

	public SettingsToggleAction( Xenon program ) {
		super( program );
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		Set<Tool> tools = getProgram().getWorkspaceManager().getActiveWorkpaneTools( SettingsTool.class );

		if( tools.isEmpty() ) {
			// Open the settings tool
			getProgram().getAssetManager().openAsset( ProgramSettingsType.URI );
		} else {
			// Close the settings tools
			tools.forEach( Tool::close );
		}
	}
}
