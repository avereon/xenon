package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AboutToolOpenUIT extends AboutToolUIT {

	@Test
	void execute() throws Exception {
		assertToolCount( getWorkpane(), 0 );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-about" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );

		assertThat( getWorkpane().getActiveTool() ).isInstanceOf( AboutTool.class );
		assertToolCount( getWorkpane(), 2 );
	}

}
