package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.test.BaseToolUIT;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openTool() {
		clickOn( "#menu-edit" );
		clickOn( "#menuitem-settings" );
	}

}
