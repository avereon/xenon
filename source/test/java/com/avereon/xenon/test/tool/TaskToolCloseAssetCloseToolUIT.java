package com.avereon.xenon.test.tool;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramTaskType;
import com.avereon.xenon.tool.TaskTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class TaskToolCloseAssetCloseToolUIT extends TaskToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramTaskType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( pane.getActiveTool(), instanceOf( TaskTool.class ) );
		assertToolCount( pane, 1 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertToolCount( pane, 0 );
	}

}
