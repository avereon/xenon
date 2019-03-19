package com.xeomar.xenon.tool.about;

import com.xeomar.xenon.workarea.Workpane;
import com.xeomar.xenon.workarea.WorkpaneEvent;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AboutToolOpenTwiceUIT extends AboutToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		assertThat( pane.getActiveTool(), instanceOf( AboutTool.class ) );
		assertThat( pane.getTools().size(), is( 2 ) );

		// Try to open the tool again and make sure there is still only one
		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ACTIVATED );
		assertThat( pane.getTools().size(), is( 2 ) );
	}

}
