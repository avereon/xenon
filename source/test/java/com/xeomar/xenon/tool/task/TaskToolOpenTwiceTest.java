package com.xeomar.xenon.tool.task;

import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TaskToolOpenTwiceTest extends TaskToolTest {

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
