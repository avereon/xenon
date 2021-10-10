package com.avereon.xenon.test.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProgramWorkspaceTitleUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() {
		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		String workareaName = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getTitle(), is( workareaName + " - " + getProgram().getCard().getName() ) );
	}

}
