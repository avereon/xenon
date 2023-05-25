package com.avereon.xenon.tool.settings;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zarra.javafx.Fx;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class SettingsToolOpenUIT extends SettingsToolUIT {

	@Test
	void execute( FxRobot robot ) throws Exception {
		Workpane pane = getWorkpane();
		assertToolCount( pane, 0 );

		openSettingsTool( robot );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneEventWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( SettingsTool.class );
		assertToolCount( pane, 2 );
	}

}
