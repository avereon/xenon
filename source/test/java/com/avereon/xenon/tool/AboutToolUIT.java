package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;

abstract class AboutToolUIT extends BaseToolUIT {

	void openAboutTool() throws Exception {
		openMenuItem( "#menu-help", "#menuitem-about" );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
