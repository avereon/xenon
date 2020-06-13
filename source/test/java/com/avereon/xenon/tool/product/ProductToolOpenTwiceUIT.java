package com.avereon.xenon.tool.product;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

class ProductToolOpenTwiceUIT extends ProductToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertToolCount( pane, 0 );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-product" );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool(), instanceOf( ProductTool.class ) );
		assertToolCount( pane, 2 );

		// Try to open the tool again and make sure there is still only one
		clickOn( "#menu-help" );
		clickOn( "#menuitem-product" );
		workpaneWatcher.waitForEvent( ToolEvent.ACTIVATED );
		assertToolCount( pane, 2 );
	}

}
