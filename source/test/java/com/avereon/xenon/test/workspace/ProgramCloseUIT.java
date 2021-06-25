package com.avereon.xenon.test.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProgramCloseUIT extends ProgramWorkspaceUIT {

	@Test
	void execute() throws Exception {
		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing(), is( true ) );

		closeProgram();
		clickOn( "No" );
		assertThat( stage.isShowing(), is( true ) );

		closeProgram();
		clickOn( "Yes" );
		assertThat( stage.isShowing(), is( false ) );
	}

}
