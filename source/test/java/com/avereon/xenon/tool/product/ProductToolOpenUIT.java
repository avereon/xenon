package com.avereon.xenon.tool.product;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

class ProductToolOpenUIT extends ProductToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openProductTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( pane.getActiveTool() ).isInstanceOf( ProductTool.class );
		assertToolCount( pane, 2 );
	}

}
