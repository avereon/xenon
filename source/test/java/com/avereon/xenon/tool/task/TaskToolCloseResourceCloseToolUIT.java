package com.avereon.xenon.tool.task;

import com.avereon.xenon.resource.type.ProgramTaskType;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneEvent;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

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
