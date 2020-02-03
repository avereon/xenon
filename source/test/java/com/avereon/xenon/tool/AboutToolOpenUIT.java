package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class AboutToolOpenUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		assertThat( workpane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		assertThat( workpane.getTools().size(), is( 2 ) );
		assertThat( workpane.getActiveTool(), instanceOf( AboutTool.class ) );
	}

}
