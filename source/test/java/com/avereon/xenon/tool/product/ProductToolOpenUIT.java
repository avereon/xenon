package com.avereon.xenon.tool.product;

import com.avereon.xenon.workarea.Workpane;
import com.avereon.xenon.workarea.WorkpaneEvent;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProductToolOpenUIT extends ProductToolUIT {

	@Test
	public void execute() throws Exception {
		Workpane pane = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getWorkpane();
		assertThat( pane.getTools().size(), is( 0 ) );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-product" );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );
		workpaneWatcher.waitForEvent( WorkpaneEvent.Type.TOOL_ADDED );

		assertThat( pane.getTools().size(), is( 2 ) );
		assertThat( pane.getActiveTool(), instanceOf( ProductTool.class ) );
	}

}