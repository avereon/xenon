package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolOpenUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		openWelcomeTool();
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isFalse();
		assertToolCount( pane, 1 );
	}

}
