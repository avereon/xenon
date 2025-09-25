package com.avereon.xenon.tool.product;

import com.avereon.xenon.BaseToolUIT;
import com.avereon.xenon.test.ProgramTestConfig;
import com.avereon.zerra.javafx.Fx;

abstract class ProductToolUIT extends BaseToolUIT {

	void openProductTool() throws Exception {
		openMenuItem( "#menu-view", "#menu-item-product" );
		Fx.waitForWithExceptions( ProgramTestConfig.LONG_TIMEOUT );
	}

}
