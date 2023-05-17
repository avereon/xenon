package com.avereon.xenon.tool.settings;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.junit5.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.api.FxRobot;
import org.testfx.robot.Motion;

abstract class SettingsToolUIT extends BaseToolUIT {

	void openSettingsTool( FxRobot robot) throws Exception {
		robot.clickOn( "#toolitem-menu" );
		robot.clickOn( "#menu-edit" );
		robot.clickOn( "#menuitem-settings", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.TIMEOUT );
	}

}
