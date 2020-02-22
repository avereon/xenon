package com.avereon.xenon.tool.product;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class ProductToolOpenUIT extends ProductToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-product" );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );
		workpaneWatcher.waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getTools().size(), is( 2 ) );
		assertThat( pane.getActiveTool(), instanceOf( ProductTool.class ) );
	}

}
