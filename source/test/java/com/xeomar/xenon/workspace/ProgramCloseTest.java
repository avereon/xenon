package com.xeomar.xenon.workspace;

import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramCloseTest extends ProgramWorkspaceTest {

	@Test
	public void execute() {
		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		assertThat( stage.isShowing(), is( true ) );

		closeProgram();
		clickOn( "No" );
		assertThat( stage.isShowing(), is( true ) );

		closeProgram();
		clickOn( "Yes" );
		assertThat( stage.isShowing(), is( false ) );
	}

}
