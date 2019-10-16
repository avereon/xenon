package com.avereon.xenon.workspace;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProgramCloseUIT extends ProgramWorkspaceUIT {

	@Test
	public void execute() {
		Stage stage = program.getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing(), is( true ) );

		closeProgram();
		clickOn( "No" );
		assertThat( stage.isShowing(), is( true ) );

		closeProgram();
		clickOn( "Yes" );
		assertThat( stage.isShowing(), is( false ) );
	}

}
