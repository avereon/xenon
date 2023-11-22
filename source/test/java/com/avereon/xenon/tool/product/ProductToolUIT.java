package com.avereon.xenon.tool.product;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zarra.javafx.Fx;

abstract class ProductToolUIT extends BaseToolUIT {

	void openProductTool() throws Exception {
		openMenuItem( "#menu-view", "#menuitem-product" );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
