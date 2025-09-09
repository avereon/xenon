package com.avereon.xenon.tool.guide;

import com.avereon.xenon.asset.type.ProgramGuideType;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuideToolOpenUIT extends GuideToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		getProgram().getAssetManager().openAsset( ProgramGuideType.URI );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool() ).isInstanceOf( GuideTool.class );
		assertToolCount( pane, 1 );
	}

}
