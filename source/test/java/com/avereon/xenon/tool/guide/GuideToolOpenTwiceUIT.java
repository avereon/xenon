package com.avereon.xenon.tool.guide;

import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class GuideToolOpenTwiceUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		program.getAssetManager().openAsset( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertToolCount( pane, 1 );

		// Try to open the tool again and make sure there is still only one

		program.getAssetManager().openAsset( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ACTIVATED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertToolCount( pane, 1 );
	}

}
