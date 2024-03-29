package com.avereon.xenon.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XenonWorkspaceTitleUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() {
		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		String workareaName = getProgram().getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();
		assertThat( stage.isShowing() ).isTrue();
		assertThat( stage.getTitle() ).isEqualTo( workareaName + " - " + getProgram().getCard().getName() );
	}

}
