package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.BaseToolUIT;

abstract class SettingsToolUIT extends BaseToolUIT {

	protected void openTool() {
		clickOn( "#menu-program" );
		clickOn( "#menuitem-settings" );
	}

}
