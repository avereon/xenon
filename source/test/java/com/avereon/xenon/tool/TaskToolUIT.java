package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.robot.Motion;

abstract class TaskToolUIT extends BaseToolUIT {

	void openTaskTool() throws Exception {
		robot.clickOn( "#toolitem-menu" );
		robot.clickOn( "#menu-view" );
		robot.clickOn( "#menuitem-task", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
