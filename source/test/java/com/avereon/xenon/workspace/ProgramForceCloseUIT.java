package com.avereon.xenon.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProgramForceCloseUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() {
		Stage stage = program.getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing(), is( true ) );

		closeProgram( true );
		assertThat( stage.isShowing(), is( false ) );
	}

}
