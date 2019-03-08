package com.xeomar.xenon.workspace;

import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramForceCloseTest extends ProgramWorkspaceTest {

	@Test
	public void execute() {
		Stage stage = program.getWorkspaceManager().getActiveStage();
		assertThat( stage.isShowing(), is( true ) );

		closeProgram( true );
		assertThat( stage.isShowing(), is( false ) );
	}

}
