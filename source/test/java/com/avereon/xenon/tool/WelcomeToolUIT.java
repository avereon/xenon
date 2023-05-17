package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.junit5.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.api.FxRobot;
import org.testfx.robot.Motion;

abstract class WelcomeToolUIT extends BaseToolUIT {

	protected void openWelcomeTool( FxRobot robot) throws Exception {
		robot.clickOn( "#toolitem-menu" );
		robot.clickOn( "#menu-help" );
		robot.clickOn( "#menuitem-welcome", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.TIMEOUT );
	}

}
