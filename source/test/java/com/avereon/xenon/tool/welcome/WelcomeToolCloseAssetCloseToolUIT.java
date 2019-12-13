package com.avereon.xenon.tool.welcome;

import com.avereon.xenon.asset.type.ProgramWelcomeType;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.xenon.workpane.WorkpaneEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class WelcomeToolCloseAssetCloseToolUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		Future<ProgramTool> future = program.getAssetManager().open( ProgramWelcomeType.URI );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( WelcomeTool.class ) );
		assertThat( pane.getActiveView().isMaximized(), is( true ) );
		assertThat( pane.getTools().size(), is( 1 ) );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_REMOVED );
		assertThat( pane.getMaximizedView(), is( nullValue() ) );
		assertThat( pane.getTools().size(), is( 0 ) );
	}

}