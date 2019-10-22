package com.avereon.xenon.tool.guide;

import com.avereon.xenon.resource.type.ProgramGuideType;
import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneEvent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class GuideToolOpenTwiceUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );

		// Try to open the tool again and make sure there is still only one

		program.getResourceManager().open( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

}
