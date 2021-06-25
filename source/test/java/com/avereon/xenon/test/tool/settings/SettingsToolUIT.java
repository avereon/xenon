package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.test.BaseToolUIT;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openTool() {
		clickOn( "#menu-help" );
		clickOn( "#menuitem-settings" );
	}

}
