package com.avereon.xenon.tool.settings;

import com.avereon.xenon.BaseToolUIT;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openTool() {
		clickOn( "#menu-file" );
		clickOn( "#menuitem-settings" );
	}

}
