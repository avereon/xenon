package com.xeomar.xenon.tool.product;

import com.xeomar.xenon.resource.type.ProgramProductType;
import com.xeomar.xenon.tool.ProgramTool;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProductToolCloseResourceCloseToolUIT extends ProductToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		Future<ProgramTool> future = program.getResourceManager().open( ProgramProductType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( ProductTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		program.getResourceManager().closeResources( future.get().getResource() );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

}
