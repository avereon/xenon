package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.robot.Motion;

abstract class AboutToolUIT extends BaseToolUIT {

	void openAboutTool() throws Exception {
		robot.clickOn( "#toolitem-menu" );
		robot.clickOn( "#menu-help" );
		robot.clickOn( "#menuitem-about", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
