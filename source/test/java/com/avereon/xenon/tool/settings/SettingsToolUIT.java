package com.avereon.xenon.tool.settings;

import com.avereon.xenon.BaseToolUIT;

abstract class SettingsToolUIT extends BaseToolUIT {

	protected void openTool() {
		clickOn( "#menu-program" );
		clickOn( "#menuitem-settings" );
	}

}
