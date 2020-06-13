package com.avereon.xenon.tool.settings;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class SettingsToolOpenUIT extends SettingsToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		openTool();
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( SettingsTool.class ) );
		assertToolCount( pane, 2 );
	}

}
