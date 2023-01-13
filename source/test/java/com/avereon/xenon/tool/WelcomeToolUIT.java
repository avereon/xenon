package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.robot.Motion;

abstract class WelcomeToolUIT extends BaseToolUIT {

	protected void openWelcomeTool() throws Exception {
		clickOn( "#toolitem-program" );
		clickOn( "#menu-help" );
		clickOn( "#menuitem-welcome", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.TIMEOUT );
	}

}
