package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

class TaskToolOpenUIT extends TaskToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openTaskTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( TaskTool.class );
		assertToolCount( pane, 1 );
	}

}
