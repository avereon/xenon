package com.avereon.xenon.tool.product;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.api.FxRobot;
import org.testfx.robot.Motion;

abstract class ProductToolUIT extends BaseToolUIT {

	void openProductTool( FxRobot robot ) throws Exception {
		robot.clickOn( "#toolitem-menu" );
		robot.clickOn( "#menu-view" );
		robot.clickOn( "#menuitem-product", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.TIMEOUT );
	}

}
