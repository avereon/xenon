package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;

abstract class TaskToolUIT extends BaseToolUIT {

	void openTaskTool() throws Exception {
		openMenuItem( "#menu-view", "#menu-item-task" );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
