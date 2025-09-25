package com.avereon.xenon.tool.product;

import com.avereon.xenon.tool.settings.SettingsTool;
import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductToolOpenTwiceUIT extends ProductToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		openProductTool();
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool() ).isInstanceOf( SettingsTool.class );
		assertToolCount( pane, 2 );

		// Try to open the tool again and make sure there is still only one
		openProductTool();
		getWorkpaneWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertToolCount( pane, 2 );
	}

}
