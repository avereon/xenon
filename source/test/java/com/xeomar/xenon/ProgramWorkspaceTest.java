package com.xeomar.xenon;

import javafx.stage.Stage;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramWorkspaceTest extends FxProgramTestCase {

	@Test
	public void testWorkspaceWindowTitle() throws Exception {
		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		String workareaName = program.getWorkspaceManager().getActiveWorkspace().getActiveWorkarea().getName();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getTitle(), is( workareaName + " - " + metadata.getName() ) );
	}

	@Test
	public void testWorkspaceDefaultSceneSize() throws Exception {
		Stage stage = program.getWorkspaceManager().getActiveWorkspace().getStage();
		assertThat( stage.isShowing(), is( true ) );
		assertThat( stage.getScene().getWidth(), is( 960d ) );
		assertThat( stage.getScene().getHeight(), is( 540d ) );
	}

}
