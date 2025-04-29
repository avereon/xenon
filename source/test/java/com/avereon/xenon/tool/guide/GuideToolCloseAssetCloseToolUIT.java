package com.avereon.xenon.tool.guide;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workspace.Workarea;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class GuideToolCloseAssetCloseToolUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workarea area = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea();
		assertToolCount( area, 0 );

		Future<ProgramTool> future = getProgram().getAssetManager().openAsset( ProgramGuideType.URI );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( area.getActiveTool() ).isInstanceOf( GuideTool.class );
		assertToolCount( area, 1 );

		getProgram().getAssetManager().closeAssets( future.get().getAsset() );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.REMOVED );
		assertToolCount( area, 0 );
	}

}
