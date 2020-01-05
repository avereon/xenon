package com.avereon.xenon.tool.settings;

import com.avereon.xenon.asset.type.ProgramSettingsType;
import com.avereon.xenon.tool.ProgramTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class SettingsToolCloseAssetCloseToolUIT extends SettingsToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		Future<ProgramTool> future = program.getAssetManager().openAsset( ProgramSettingsType.URI );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( SettingsTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		program.getAssetManager().closeAssets( future.get().getAsset() );
		workpaneWatcher.waitForEvent( ToolEvent.REMOVED );
		assertThat( pane.getTools().size(), is( 1 ) );
	}

}
