package com.xeomar.xenon.tool.guide;

import com.xeomar.xenon.resource.type.ProgramGuideType;
import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GuideToolOpenUIT extends GuideToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		program.getResourceManager().open( ProgramGuideType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( GuideTool.class ) );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

}
