package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class TaskToolOpenUIT extends TaskToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		openTaskMenu();
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertToolCount( pane, 1 );
	}

}
