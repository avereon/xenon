package com.avereon.xenon.tool;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;

import static com.avereon.xenon.test.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class WelcomeToolOpenUIT extends WelcomeToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		clickOn( "#toolitem-program" );
		clickOn( "#menu-help" );
		clickOn( "#menuitem-welcome" );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( WelcomeTool.class );
		assertThat( pane.getActiveView().isMaximized() ).isTrue();
		assertToolCount( pane, 1 );
	}

}
