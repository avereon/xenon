package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.BaseToolTestCase;

abstract class SettingsToolTest extends BaseToolTestCase {

	protected void openTool() {
		clickOn( "#menu-program" );
		clickOn( "#menuitem-settings" );
	}

}
