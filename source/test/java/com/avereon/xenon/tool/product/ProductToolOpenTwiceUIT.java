package com.avereon.xenon.tool.product;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductToolOpenTwiceUIT extends ProductToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openProductTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool() ).isInstanceOf( ProductTool.class );
		assertToolCount( pane, 2 );

		// Try to open the tool again and make sure there is still only one
		openProductTool();
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertToolCount( pane, 2 );
	}

}
