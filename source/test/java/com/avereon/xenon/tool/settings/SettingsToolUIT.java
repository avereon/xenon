package com.avereon.xenon.tool.settings;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.robot.Motion;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openSettingsTool() throws Exception {
		clickOn( "#toolitem-program" );
		clickOn( "#menu-edit" );
		clickOn( "#menuitem-settings", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.TIMEOUT );
	}

}
