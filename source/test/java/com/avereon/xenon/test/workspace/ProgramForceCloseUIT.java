package com.avereon.xenon.test.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramForceCloseUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() throws Exception {
		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing() ).isTrue();

		closeProgram( true );
		assertThat( stage.isShowing() ).isFalse();
	}

}
