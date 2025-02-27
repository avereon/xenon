package com.avereon.xenon.tool.settings;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openSettingsTool() throws Exception {
		openMenuItem( "#menu-file", "#menu-item-settings" );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
