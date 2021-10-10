package com.avereon.xenon.test.tool.settings;

import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class SettingsToolOpenTwiceUIT extends SettingsToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( pane.getActiveTool(), instanceOf( SettingsTool.class ) );
		assertToolCount( pane, 2 );

		openTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertToolCount( pane, 2 );
	}

}
