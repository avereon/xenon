package com.avereon.xenon.tool;

import com.avereon.xenon.BaseToolUIT;

abstract class TaskToolUIT extends BaseToolUIT {

	void openTaskMenu() {
		clickOn( "#menu-help" );
		clickOn( "#menu-tools" );
		clickOn( "#menuitem-task" );
	}

}
