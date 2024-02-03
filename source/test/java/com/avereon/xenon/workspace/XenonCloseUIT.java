package com.avereon.xenon.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import static org.assertj.core.api.Assertions.assertThat;

class XenonCloseUIT extends ProgramWorkspaceUIT {

	@Test
	void execute( FxRobot robot) throws Exception {
		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing() ).isTrue();

		closeProgram();
		robot.clickOn( "No" );
		assertThat( stage.isShowing() ).isTrue();

		closeProgram();
		robot.clickOn( "Yes" );
		assertThat( stage.isShowing() ).isFalse();
	}

}
