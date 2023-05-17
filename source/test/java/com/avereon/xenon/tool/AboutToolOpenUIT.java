package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolOpenUIT extends AboutToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		assertToolCount( getWorkpane(), 0 );

		openAboutTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( getWorkpane().getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( getWorkpane(), 2 );
	}

}
