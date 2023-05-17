package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolOpenTwiceUIT extends WelcomeToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openWelcomeTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isTrue();
		assertToolCount( pane, 1 );

		openWelcomeTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );

		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isTrue();
		assertToolCount( pane, 1 );
	}

}
