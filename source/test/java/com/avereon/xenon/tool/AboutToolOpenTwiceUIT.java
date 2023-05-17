package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolOpenTwiceUIT extends AboutToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openAboutTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );

		// Try to open the tool again and make sure there is still only one
		openAboutTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );

		assertToolCount( pane, 2 );
	}

}
