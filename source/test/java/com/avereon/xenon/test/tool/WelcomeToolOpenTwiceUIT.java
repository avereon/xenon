package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolOpenTwiceUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-welcome" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isTrue();
		assertToolCount( pane, 1 );

		clickOn( "#menu-help" );
		clickOn( "#menuitem-welcome" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isTrue();
		assertToolCount( pane, 1 );
	}

}
