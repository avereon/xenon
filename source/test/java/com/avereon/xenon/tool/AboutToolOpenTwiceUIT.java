package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolOpenTwiceUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( pane, 2 );

		// Try to open the tool again and make sure there is still only one
		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertToolCount( pane, 2 );
	}

}
