package com.avereon.xenon.tool.settings;

import com.avereon.xenon.workpane.ToolEvent;
import com.avereon.xenon.workpane.Workpane;
import com.avereon.zerra.javafx.Fx;
import org.junit.jupiter.api.Test;

import static com.avereon.xenon.test.ProgramTestConfig.LONG_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

class SettingsToolOpenTwiceUIT extends SettingsToolUIT {

	@Test
	void execute() throws Exception {
		Workpane pane = getWorkarea();
		assertToolCount( pane, 0 );

		openSettingsTool();
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		getWorkpaneWatcher().waitForEvent( ToolEvent.ADDED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertThat( pane.getActiveTool() ).isInstanceOf( SettingsTool.class );
		assertToolCount( pane, 2 );

		openSettingsTool();
		getWorkpaneWatcher().waitForEvent( ToolEvent.ACTIVATED );
		Fx.waitForWithExceptions( LONG_TIMEOUT );
		assertToolCount( pane, 2 );
	}

}
