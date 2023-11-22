package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;

abstract class WelcomeToolUIT extends BaseToolUIT {

	protected void openWelcomeTool() throws Exception {
		openMenuItem( "#menu-help", "#menuitem-welcome" );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
