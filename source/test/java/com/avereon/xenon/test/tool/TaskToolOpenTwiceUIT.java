package com.avereon.xenon.test.tool;

import com.avereon.xenon.tool.TaskTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class TaskToolOpenTwiceUIT extends TaskToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openTaskMenu();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertToolCount( pane, 1 );

		// Try to open the tool again and make sure there is still only one
		openTaskMenu();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertToolCount( pane, 1 );
	}

}
