package com.avereon.xenon.test.tool;

import com.avereon.xenon.test.BaseToolUIT;

abstract class TaskToolUIT extends BaseToolUIT {

	void openTaskMenu() {
		clickOn( "#menu-view" );
		clickOn( "#menuitem-task" );
	}

}
