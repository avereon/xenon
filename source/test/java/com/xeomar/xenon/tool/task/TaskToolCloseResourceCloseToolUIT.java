package com.xeomar.xenon.tool.task;

import com.xeomar.xenon.resource.type.ProgramTaskType;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TaskToolCloseResourceCloseToolUIT extends TaskToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), Matchers.is( 0 ) );

		Future<ProgramTool> future = program.getResourceManager().open( ProgramTaskType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertThat( pane.getTools().size(), Matchers.is( 1 ) );

		program.getResourceManager().closeResources( future.get().getResource() );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
		assertThat( pane.getTools().size(), Matchers.is( 0 ) );
	}

}
