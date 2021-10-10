package com.avereon.xenon.test.tool;

import com.avereon.xenon.tool.AboutTool;
import com.avereon.xenon.workpane.ToolEvent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class AboutToolOpenUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		assertToolCount( getWorkpane(), 0 );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( getWorkpane().getActiveTool(), instanceOf( AboutTool.class ) );
		assertToolCount( getWorkpane(), 2 );
	}

}
