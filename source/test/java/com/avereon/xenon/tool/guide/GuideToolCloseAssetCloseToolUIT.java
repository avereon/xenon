package com.avereon.xenon.tool.guide;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class GuideToolCloseAssetCloseToolUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		Future<ProgramTool> future = program.getAssetManager().openAsset( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertToolCount( pane, 1 );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		assertToolCount( pane, 0 );
	}

}
