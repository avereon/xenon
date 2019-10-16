package com.avereon.xenon.tool.task;

import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneEvent;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class TaskToolOpenTwiceUIT extends TaskToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), Matchers.is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-task" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertThat( pane.getTools().size(), Matchers.is( 1 ) );

		// Try to open the tool again and make sure there is still only one
		clickOn( "#menu-help" );
		clickOn( "#menuitem-task" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertThat( pane.getTools().size(), Matchers.is( 1 ) );
	}

}
