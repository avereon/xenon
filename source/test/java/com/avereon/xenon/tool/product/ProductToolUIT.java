package com.avereon.xenon.tool.product;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;
import org.testfx.robot.Motion;

abstract class ProductToolUIT extends BaseToolUIT {

	void openProductTool() throws Exception {
		clickOn( "#toolitem-menu" );
		clickOn( "#menu-view" );
		clickOn( "#menuitem-product", Motion.HORIZONTAL_FIRST );
		Fx.waitForWithExceptions( ProgramTestConfig.TIMEOUT );
	}

}
