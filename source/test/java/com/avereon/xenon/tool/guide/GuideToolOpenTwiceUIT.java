package com.avereon.xenon.tool.guide;

import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuideToolOpenTwiceUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		getProgram().getAssetManager().openAsset( ProgramGuideType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool() ).isInstanceOf( GuideTool.class );
		assertToolCount( pane, 1 );

		// Try to open the tool again and make sure there is still only one

		getProgram().getAssetManager().openAsset( ProgramGuideType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertThat( pane.getActiveTool() ).isInstanceOf( GuideTool.class );
		assertToolCount( pane, 1 );
	}

}
