package com.avereon.xenon.tool.settings;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.api.FxRobot;
import org.testfx.robot.Motion;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openSettingsTool( FxRobot robot) throws Exception {
		robot.clickOn( MAIN_MENU );
		robot.clickOn( "#menu-edit" );
		robot.clickOn( "#menuitem-settings", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
