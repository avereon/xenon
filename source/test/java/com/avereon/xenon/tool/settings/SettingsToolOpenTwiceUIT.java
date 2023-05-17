package com.avereon.xenon.tool.settings;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static com.avereon.xenon.junit5.ProgramTestConfig.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class SettingsToolOpenTwiceUIT extends SettingsToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openSettingsTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( SettingsTool.class );
		assertToolCount( pane, 2 );

		openSettingsTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ACTIVATED );
		Fx.waitForWithExceptions( TIMEOUT );
		assertToolCount( pane, 2 );
	}

}
