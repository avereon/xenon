package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolOpenTwiceUIT extends AboutToolUIT {

	@Test
	@DisabledOnOs( OS.MAC )
	void execute() throws Exception {
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		openAboutTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );

		// Try to open the tool again and make sure there is still only one
		openAboutTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );

		assertToolCount( pane, 2 );
	}

}
