package com.avereon.xenon.tool.guide;

import com.avereon.xenon.resource.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workspace.Workarea;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuideToolOpenTwiceUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workarea area = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();
		assertToolCount( area, 0 );

		getProgram().getResourceManager().openAsset( ProgramGuideType.URI );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( area.getActiveTool() ).isInstanceOf( GuideTool.class );
		assertToolCount( area, 1 );

		// Try to open the tool again and make sure there is still only one

		getProgram().getResourceManager().openAsset( ProgramGuideType.URI );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertThat( area.getActiveTool() ).isInstanceOf( GuideTool.class );
		assertToolCount( area, 1 );
	}

}
