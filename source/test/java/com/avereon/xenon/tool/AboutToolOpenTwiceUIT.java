package com.avereon.xenon.tool;

import com.avereon.log.LogLevel;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolOpenTwiceUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {

		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		// NOTE This appears to be intermittently failing on macOS but for an unknown reason.
		// Other tests of this nature also fail occasionally on macOS.

		// DEGUG This line appears to execute correctly by causing the about tool to be added to the workpane.
		// However, the guide is not added to the workpane. This is a similar problem with other guided tools,
		// when they happen to fail.
		setLogLevel( LogLevel.INFO );
		openAboutTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );
		setLogLevel( LogLevel.OFF );

		// Try to open the tool again and make sure there is still only one
		openAboutTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );

		assertToolCount( pane, 2 );
	}

}
