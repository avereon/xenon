package com.avereon.xenon.tool;

import com.avereon.xenon.asset.type.ProgramTaskType;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class TaskToolCloseAssetCloseToolUIT extends TaskToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), Matchers.is( 0 ) );

		Future<ProgramTool> future = program.getAssetManager().openAsset( ProgramTaskType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertThat( pane.getTools().size(), Matchers.is( 1 ) );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		assertThat( pane.getTools().size(), Matchers.is( 0 ) );
	}

}
