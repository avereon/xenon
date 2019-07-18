package com.avereon.xenon.tool.task;

import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneEvent;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TaskToolOpenUIT extends TaskToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), Matchers.is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-task" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );

		assertThat( pane.getTools().size(), Matchers.is( 1 ) );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
	}

}
